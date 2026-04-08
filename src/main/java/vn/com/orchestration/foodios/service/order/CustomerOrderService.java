package vn.com.orchestration.foodios.service.order;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.order.GetOrderResponse;
import vn.com.orchestration.foodios.dto.order.GetOrdersResponse;
import vn.com.orchestration.foodios.dto.order.PlaceOrderRequest;
import vn.com.orchestration.foodios.dto.order.PlaceOrderResponse;
import vn.com.orchestration.foodios.entity.order.OrderStatus;

import java.util.UUID;

public interface CustomerOrderService {

    PlaceOrderResponse placeOrder(PlaceOrderRequest request);

    GetOrderResponse getOrder(BaseRequest request, UUID orderId);

    GetOrdersResponse getOrders(BaseRequest request, OrderStatus status, Integer pageNumber, Integer pageSize);
}
