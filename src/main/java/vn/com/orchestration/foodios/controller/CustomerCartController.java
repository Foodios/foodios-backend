package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.cart.AddCartItemRequest;
import vn.com.orchestration.foodios.dto.cart.AddCartItemResponse;
import vn.com.orchestration.foodios.dto.cart.GetCartResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.cart.CustomerCartService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.CARTS_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.ITEMS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + CARTS_PATH)
@RequiredArgsConstructor
public class CustomerCartController {

    private final CustomerCartService customerCartService;

    @GetMapping
    public ResponseEntity<GetCartResponse> getCart(
            @RequestParam UUID storeId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetCartResponse response = customerCartService.getCart(baseRequest, storeId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @PostMapping(ITEMS_PATH)
    public ResponseEntity<AddCartItemResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        AddCartItemResponse response = customerCartService.addItem(request);
        return HttpUtils.buildResponse(request, response);
    }
}
