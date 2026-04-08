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
import vn.com.orchestration.foodios.dto.store.CreateStoreRequest;
import vn.com.orchestration.foodios.dto.store.CreateStoreResponse;
import vn.com.orchestration.foodios.dto.store.DeleteStoreResponse;
import vn.com.orchestration.foodios.dto.store.GetStoreResponse;
import vn.com.orchestration.foodios.dto.store.GetStoresResponse;
import vn.com.orchestration.foodios.dto.store.UpdateStoreRequest;
import vn.com.orchestration.foodios.dto.store.UpdateStoreResponse;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.merchant.MerchantStoreService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.CREATE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANT_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.STORES_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MERCHANT_PATH)
@RequiredArgsConstructor
public class MerchantStoreController {

    private final MerchantStoreService merchantStoreService;

    @PostMapping(STORES_PATH + CREATE_PATH)
    public ResponseEntity<CreateStoreResponse> createStore(@Valid @RequestBody CreateStoreRequest request) {
        CreateStoreResponse response = merchantStoreService.createStore(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PutMapping(STORES_PATH + "/{storeId}")
    public ResponseEntity<UpdateStoreResponse> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody UpdateStoreRequest request
    ) {
        UpdateStoreResponse response = merchantStoreService.updateStore(storeId, request);
        return HttpUtils.buildResponse(request, response);
    }

    @DeleteMapping(STORES_PATH + "/{storeId}")
    public ResponseEntity<DeleteStoreResponse> deleteStore(
            @PathVariable UUID storeId,
            @RequestParam UUID merchantId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        DeleteStoreResponse response = merchantStoreService.deleteStore(storeId, baseRequest, merchantId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(STORES_PATH + "/{storeId}")
    public ResponseEntity<GetStoreResponse> getStore(
            @PathVariable UUID storeId,
            @RequestParam UUID merchantId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetStoreResponse response = merchantStoreService.getStore(storeId, baseRequest, merchantId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(STORES_PATH)
    public ResponseEntity<GetStoresResponse> getStores(
            @RequestParam UUID merchantId,
            @RequestParam(required = false) StoreStatus status,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetStoresResponse response = merchantStoreService.getStores(baseRequest, merchantId, status);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
