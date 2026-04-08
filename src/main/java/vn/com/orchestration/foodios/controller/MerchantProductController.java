package vn.com.orchestration.foodios.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.product.CreateProductRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductResponse;
import vn.com.orchestration.foodios.service.catalog.MerchantProductService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.CREATE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANT_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.PRODUCTS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MERCHANT_PATH)
@RequiredArgsConstructor
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    @PostMapping(PRODUCTS_PATH + CREATE_PATH)
    public ResponseEntity<CreateProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductResponse response = merchantProductService.createProduct(request);
        return HttpUtils.buildResponse(request, response);
    }

//
//    @PostMapping(PRODUCTS_PATH + UPDATE_PATH)
//    public ResponseEntity<UpdateProductResponse> updateProduct(@Valid @RequestBody UpdateProductRequest request) {
//
//    }
}
