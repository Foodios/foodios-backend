package vn.com.orchestration.foodios.service.order;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.order.GetOrderResponse;
import vn.com.orchestration.foodios.dto.order.GetOrdersResponse;
import vn.com.orchestration.foodios.dto.order.UpdateOrderStatusResponse;
import vn.com.orchestration.foodios.entity.order.OrderStatus;

import java.util.UUID;

public interface MerchantOrderService {

    GetOrdersResponse getOrders(BaseRequest request, UUID merchantId, OrderStatus status, String query, Integer pageNumber, Integer pageSize);

    GetOrderResponse getOrder(BaseRequest request, UUID merchantId, UUID orderId);

    UpdateOrderStatusResponse updateOrderStatus(BaseRequest baseRequest, UUID merchantId, UUID orderId, OrderStatus status);
    
    GetOrdersResponse getOrderHistory(BaseRequest request, UUID merchantId, Integer pageNumber, Integer pageSize);

    UpdateOrderStatusResponse markAsDelivered(BaseRequest baseRequest, UUID merchantId, UUID orderId);
}
