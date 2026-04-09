package vn.com.orchestration.foodios.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.admin.GetAdminOrdersResponse;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.OrderRepository;
import vn.com.orchestration.foodios.service.admin.AdminOrderService;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static vn.com.orchestration.foodios.constant.ErrorConstant.ADMIN_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final IdentityUserContextProvider identityUserContextProvider;

    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_PLATFORM_ADMIN");

    @Override
    @Transactional(readOnly = true)
    public GetAdminOrdersResponse getTodayOrders(BaseRequest request, OrderStatus status, String query) {
        authorizeAdmin(request);
        String keyword = query == null ? "" : query.trim().toLowerCase();

        OffsetDateTime startOfToday = OffsetDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        List<FoodOrder> todayOrders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt().isAfter(startOfToday) || o.getCreatedAt().isEqual(startOfToday))
                .collect(Collectors.toList());

        long newOrders = todayOrders.stream().filter(o -> o.getStatus() == OrderStatus.PLACED).count();
        long preparingOrders = todayOrders.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING).count();
        long onDeliveryOrders = todayOrders.stream().filter(o -> o.getStatus() == OrderStatus.OUT_FOR_DELIVERY).count();
        long cancelledOrders = todayOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        List<FoodOrder> filteredOrders = todayOrders.stream()
                .filter(o -> status == null || o.getStatus() == status)
                .filter(o -> keyword.isEmpty() 
                        || o.getCode().toLowerCase().contains(keyword) 
                        || (o.getDeliveryAddress() != null && o.getDeliveryAddress().getContactName() != null && o.getDeliveryAddress().getContactName().toLowerCase().contains(keyword))
                        || (o.getCustomer() != null && o.getCustomer().getFullName() != null && o.getCustomer().getFullName().toLowerCase().contains(keyword)))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());

        return GetAdminOrdersResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetAdminOrdersResponse.GetAdminOrdersResponseData.builder()
                        .items(filteredOrders.stream().map(this::mapOrderPayload).collect(Collectors.toList()))
                        .totalItems((long) filteredOrders.size())
                        .numberOfNewOrders(newOrders)
                        .numberOfPreparingOrder(preparingOrders)
                        .numberOfOnDelivery(onDeliveryOrders)
                        .numberOfCancelled(cancelledOrders)
                        .build())
                .build();
    }

    private GetAdminOrdersResponse.OrderPayload mapOrderPayload(FoodOrder order) {
        String address = "";
        if (order.getDeliveryAddress() != null) {
            address = String.format("%s, %s, %s", 
                order.getDeliveryAddress().getLine1(),
                order.getDeliveryAddress().getDistrict(),
                order.getDeliveryAddress().getCity());
        }

        return GetAdminOrdersResponse.OrderPayload.builder()
                .id(order.getId())
                .code(order.getCode())
                .customerName(order.getCustomer().getFullName())
                .merchantName(order.getStore().getMerchant().getDisplayName())
                .storeName(order.getStore().getName())
                .total(order.getTotal())
                .status(order.getStatus().name())
                .serviceMethod(order.getServiceMethod() != null ? order.getServiceMethod().name() : null)
                .deliveryAddress(address)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private void authorizeAdmin(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        Set<String> roles = currentUser.roles();
        if (roles == null || roles.stream().noneMatch(ADMIN_ROLES::contains)) {
            throw new BusinessException(
                    request.getRequestId(),
                    request.getRequestDateTime(),
                    request.getChannel(),
                    ExceptionUtils.buildResultResponse(INVALID_INPUT_ERROR, ADMIN_ACCESS_DENIED_MESSAGE)
            );
        }
    }
}
