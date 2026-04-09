package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.admin.GetAdminOrdersResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.admin.AdminOrderService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.ADMIN_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;

@RestController
@RequestMapping(API_PATH + API_VERSION + ADMIN_PATH + "/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<GetAdminOrdersResponse> getTodayOrders(
            HttpServletRequest request,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String query) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetAdminOrdersResponse response = adminOrderService.getTodayOrders(baseRequest, status, query);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
