package vn.com.orchestration.foodios.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupRequest;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupResponse;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.service.merchant.MerchantManagementService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANTS_PATH;
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
}
