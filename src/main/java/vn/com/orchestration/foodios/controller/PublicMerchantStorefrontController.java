package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.storefront.GetPublicMerchantsResponse;
import vn.com.orchestration.foodios.dto.storefront.GetMerchantStorefrontResponse;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.storefront.PublicMerchantStorefrontService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANTS_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.PUBLIC_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + PUBLIC_PATH + MERCHANTS_PATH)
@RequiredArgsConstructor
public class PublicMerchantStorefrontController {

    private final PublicMerchantStorefrontService publicMerchantStorefrontService;

    @GetMapping
    public ResponseEntity<GetPublicMerchantsResponse> getPublicMerchants(HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetPublicMerchantsResponse response = publicMerchantStorefrontService.getPublicMerchants(baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<GetMerchantStorefrontResponse> getMerchantStorefront(
            @PathVariable String slug,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMerchantStorefrontResponse response =
                publicMerchantStorefrontService.getMerchantStorefront(baseRequest, slug);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
