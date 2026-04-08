package vn.com.orchestration.foodios.service.order.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.order.GetOrderResponse;
import vn.com.orchestration.foodios.dto.order.GetOrdersResponse;
import vn.com.orchestration.foodios.dto.order.OrderPayload;
import vn.com.orchestration.foodios.dto.order.UpdateOrderStatusResponse;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.order.OrderItem;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.entity.order.Payment;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.OrderItemRepository;
import vn.com.orchestration.foodios.repository.OrderRepository;
import vn.com.orchestration.foodios.repository.PaymentRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.order.MerchantOrderService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_NUMBER;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_SIZE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class MerchantOrderServiceImpl implements MerchantOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final UserRepository userRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional(readOnly = true)
    public GetOrdersResponse getOrders(BaseRequest request, UUID merchantId, OrderStatus status, Integer pageNumber, Integer pageSize) {
        validatePagination(request, pageNumber, pageSize);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());

        PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<FoodOrder> orders = status == null
                ? orderRepository.findByStoreMerchantId(merchantId, pageable)
                : orderRepository.findByStoreMerchantIdAndStatus(merchantId, status, pageable);

        return GetOrdersResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetOrdersResponse.GetOrdersResponseData.builder()
                        .items(orders.getContent().stream().map(this::toPayload).toList())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalItems(orders.getTotalElements())
                        .totalPages(orders.getTotalPages())
                        .hasNext(orders.hasNext())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrderResponse getOrder(BaseRequest request, UUID merchantId, UUID orderId) {
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());

        FoodOrder order = orderRepository.findById(orderId)
                .filter(item -> item.getStore().getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);

        return GetOrderResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(CustomerOrderMapper.toPayload(order, items, payment))
                .build();
    }

    @Override
    @Transactional
    public UpdateOrderStatusResponse updateOrderStatus(BaseRequest request, UUID merchantId, UUID orderId, OrderStatus status) {
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());

        FoodOrder order = orderRepository.findById(orderId)
                .filter(item -> item.getStore().getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Order not found"));

        if (status == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Status is required");
        }

        OffsetDateTime now = OffsetDateTime.now();

        order.setStatus(status);
        order.setUpdatedAt(now);

        orderRepository.save(order);

        return UpdateOrderStatusResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(UpdateOrderStatusResponse.UpdateOrderStatusResponseData
                        .builder()
                        .status(order.getStatus())
                        .processedAt(now)
                        .creator(currentUser.getFullName())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrdersResponse getOrderHistory(BaseRequest request, UUID merchantId, Integer pageNumber, Integer pageSize) {
        validatePagination(request, pageNumber, pageSize);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());

        PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<OrderStatus> historyStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);
        Page<FoodOrder> orders = orderRepository.findByStoreMerchantIdAndStatusIn(merchantId, historyStatuses, pageable);

        return GetOrdersResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetOrdersResponse.GetOrdersResponseData.builder()
                        .items(orders.getContent().stream().map(this::toPayload).toList())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalItems(orders.getTotalElements())
                        .totalPages(orders.getTotalPages())
                        .hasNext(orders.hasNext())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public UpdateOrderStatusResponse markAsDelivered(BaseRequest request, UUID merchantId, UUID orderId) {
        return updateOrderStatus(request, merchantId, orderId, OrderStatus.DELIVERED);
    }

    private void validatePagination(BaseRequest request, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 1) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_NUMBER);
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_SIZE);
        }
    }

    private User resolveCurrentUser(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isBlank()) {
            return userRepository.findByEmail(currentUser.email())
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        }
        try {
            UUID userId = UUID.fromString(currentUser.subject());
            return userRepository.findById(userId)
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        } catch (IllegalArgumentException exception) {
            throw businessException(request, INVALID_INPUT_ERROR, "Invalid current user");
        }
    }

    private void authorizeMerchantAccess(BaseRequest request, UUID merchantId, UUID userId) {
        boolean hasAccess = merchantMemberRepository.existsByMerchantIdAndUserIdAndStatusAndRoleIn(
                merchantId,
                userId,
                MerchantMemberStatus.ACTIVE,
                List.of(MerchantMemberRole.OWNER, MerchantMemberRole.MANAGER)
        );
        if (!hasAccess) {
            throw businessException(request, INVALID_INPUT_ERROR, MERCHANT_ACCESS_DENIED_MESSAGE);
        }
    }

    private OrderPayload toPayload(FoodOrder order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        return CustomerOrderMapper.toPayload(order, items, payment);
    }

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }
}
