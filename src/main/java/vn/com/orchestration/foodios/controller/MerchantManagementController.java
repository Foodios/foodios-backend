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
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.GetMyMerchantResponse;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupRequest;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupResponse;
import vn.com.orchestration.foodios.dto.merchant.SearchMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.SearchMerchantResponse;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.service.merchant.MerchantManagementService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANTS_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.ME_PROFILE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.SEARCH_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.SIGNUP_PATH;

@RestController
@RequestMapping( API_PATH + API_VERSION + MERCHANTS_PATH)
@RequiredArgsConstructor
public class MerchantManagementController {

    private final MerchantManagementService merchantManagementService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @PostMapping(SIGNUP_PATH)
    public ResponseEntity<MerchantSignupResponse> signupMerchant(@Valid @RequestBody MerchantSignupRequest request) {
        sLog.info("[MERCHANT-INFO] Signup Merchant Request: {}" ,request);
        MerchantSignupResponse response = merchantManagementService.signup(request);
        sLog.info("[MERCHANT-INFO] Signup Merchant Response: {}" ,response);
        return HttpUtils.buildResponse(request, response);
    }

    @GetMapping(ME_PROFILE_PATH)
    public ResponseEntity<GetMyMerchantResponse> getMyMerchant(HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMyMerchantResponse response = merchantManagementService.getMyMerchant(baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(SEARCH_PATH)
    public ResponseEntity<SearchMerchantResponse> searchMerchants(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        SearchMerchantRequest searchRequest = SearchMerchantRequest.builder()
                .requestId(baseRequest.getRequestId())
                .requestDateTime(baseRequest.getRequestDateTime())
                .channel(baseRequest.getChannel())
                .name(name)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();

        sLog.info("[MERCHANT-INFO] Search Merchants Request: {}", searchRequest);
        SearchMerchantResponse response = merchantManagementService.search(searchRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
