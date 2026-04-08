package vn.com.orchestration.foodios.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantResponse;
import vn.com.orchestration.foodios.service.merchant.AdminMerchantService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.ADMIN_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_PREFIX;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANTS_PATH;

@RestController
@RequestMapping(API_PREFIX + ADMIN_PATH + MERCHANTS_PATH)
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @PostMapping
    public ResponseEntity<CreateMerchantResponse> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        CreateMerchantResponse response = adminMerchantService.createMerchant(request);
        return HttpUtils.buildResponse(request, response);
    }
}
