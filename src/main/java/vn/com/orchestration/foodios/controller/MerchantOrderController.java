package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.order.GetOrderResponse;
import vn.com.orchestration.foodios.dto.order.GetOrdersResponse;
import vn.com.orchestration.foodios.dto.order.UpdateOrderStatusResponse;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.order.MerchantOrderService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANT_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MERCHANT_PATH + "/orders")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrder(
            @PathVariable UUID orderId,
            @RequestParam UUID merchantId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetOrderResponse response = merchantOrderService.getOrder(baseRequest, merchantId, orderId);
        return HttpUtils.buildResponse(baseRequest, response);
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<UpdateOrderStatusResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam UUID merchantId,
            @RequestParam OrderStatus status,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        UpdateOrderStatusResponse response = merchantOrderService.updateOrderStatus(baseRequest, merchantId, orderId, status);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @PutMapping("/{orderId}/mark-delivered")
    public ResponseEntity<UpdateOrderStatusResponse> markAsDelivered(
            @PathVariable UUID orderId,
            @RequestParam UUID merchantId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        UpdateOrderStatusResponse response = merchantOrderService.markAsDelivered(baseRequest, merchantId, orderId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping("/history")
    public ResponseEntity<GetOrdersResponse> getOrderHistory(
            @RequestParam UUID merchantId,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetOrdersResponse response = merchantOrderService.getOrderHistory(baseRequest, merchantId, pageNumber, pageSize);
        return HttpUtils.buildResponse(baseRequest, response);
    }


    @GetMapping
    public ResponseEntity<GetOrdersResponse> getOrders(
            @RequestParam UUID merchantId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetOrdersResponse response = merchantOrderService.getOrders(baseRequest, merchantId, status, pageNumber, pageSize);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
