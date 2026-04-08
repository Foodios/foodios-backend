package vn.com.orchestration.foodios.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.AddMerchantDriverRequest;
import vn.com.orchestration.foodios.dto.merchant.AddMerchantDriverResponse;
import vn.com.orchestration.foodios.dto.merchant.DeleteMerchantDriverResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantDriversResponse;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.service.merchant.MerchantMemberService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.CREATE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.DRIVERS_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANT_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MERCHANT_PATH + DRIVERS_PATH)
@RequiredArgsConstructor
public class MerchantDriverController {

    private final MerchantMemberService merchantMemberService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @PostMapping(CREATE_PATH)
    public ResponseEntity<AddMerchantDriverResponse> addDriver(@Valid @RequestBody AddMerchantDriverRequest request) {
        sLog.info("[MERCHANT-DRIVER] Add Driver Request: {}", request);
        AddMerchantDriverResponse response = merchantMemberService.addDriver(request);
        sLog.info("[MERCHANT-DRIVER] Add Driver Response: {}", response);
        return HttpUtils.buildResponse(request, response);
    }

    @GetMapping
    public ResponseEntity<GetMerchantDriversResponse> getDrivers(@RequestParam UUID merchantId, HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMerchantDriversResponse response = merchantMemberService.getDrivers(merchantId, baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<DeleteMerchantDriverResponse> deleteDriver(@PathVariable UUID userId, @RequestParam UUID merchantId, HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        DeleteMerchantDriverResponse response = merchantMemberService.deleteDriver(merchantId, userId, baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
