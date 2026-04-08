package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.order.GetOrderResponse;
import vn.com.orchestration.foodios.dto.order.GetOrdersResponse;
import vn.com.orchestration.foodios.dto.order.PlaceOrderRequest;
import vn.com.orchestration.foodios.dto.order.PlaceOrderResponse;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.order.CustomerOrderService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.MY_ORDERS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MY_ORDERS_PATH)
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    @GetMapping
    public ResponseEntity<GetOrdersResponse> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetOrdersResponse response = customerOrderService.getOrders(baseRequest, status, pageNumber, pageSize);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrder(
            @PathVariable UUID orderId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetOrderResponse response = customerOrderService.getOrder(baseRequest, orderId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @PostMapping("/place")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        PlaceOrderResponse response = customerOrderService.placeOrder(request);
        return HttpUtils.buildResponse(request, response);
    }
}
