package vn.com.orchestration.foodios.service.admin;

import vn.com.orchestration.foodios.dto.admin.GetAdminOrdersResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.order.OrderStatus;

public interface AdminOrderService {
    GetAdminOrdersResponse getTodayOrders(BaseRequest request, OrderStatus status, String query);
}
