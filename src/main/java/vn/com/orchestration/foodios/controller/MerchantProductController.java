package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductRequest;
import vn.com.orchestration.foodios.dto.product.CreateProductResponse;
import vn.com.orchestration.foodios.dto.product.DeleteProductResponse;
import vn.com.orchestration.foodios.dto.product.GetProductResponse;
import vn.com.orchestration.foodios.dto.product.GetProductsResponse;
import vn.com.orchestration.foodios.dto.product.UpdateProductRequest;
import vn.com.orchestration.foodios.dto.product.UpdateProductResponse;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.catalog.MerchantProductService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

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

    @PutMapping(PRODUCTS_PATH + "/{productId}")
    public ResponseEntity<UpdateProductResponse> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        UpdateProductResponse response = merchantProductService.updateProduct(productId, request);
        return HttpUtils.buildResponse(request, response);
    }

    @DeleteMapping(PRODUCTS_PATH + "/{productId}")
    public ResponseEntity<DeleteProductResponse> deleteProduct(
            @PathVariable UUID productId,
            @RequestParam UUID storeId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        DeleteProductResponse response = merchantProductService.deleteProduct(productId, baseRequest, storeId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(PRODUCTS_PATH + "/{productId}")
    public ResponseEntity<GetProductResponse> getProduct(
            @PathVariable UUID productId,
            @RequestParam UUID storeId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetProductResponse response = merchantProductService.getProduct(productId, baseRequest, storeId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(PRODUCTS_PATH)
    public ResponseEntity<GetProductsResponse> getProducts(
            @RequestParam UUID storeId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String query,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetProductsResponse response = merchantProductService.getProducts(baseRequest, storeId, categoryId, status, query);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
