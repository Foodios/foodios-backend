package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.*;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.merchant.AdminMerchantService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.ADMIN_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.APPLICATIONS_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.APPROVE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANTS_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.REJECT_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.UPDATE_PATH;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping(API_PATH + API_VERSION + ADMIN_PATH + MERCHANTS_PATH)
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @GetMapping
    public ResponseEntity<GetMerchantsResponse> getMerchants(
            HttpServletRequest request,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMerchantsResponse response = adminMerchantService.getMerchants(baseRequest, query, pageNumber, pageSize);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetMerchantDetailResponse> getMerchantDetail(@PathVariable UUID id, HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMerchantDetailResponse response = adminMerchantService.getMerchantDetail(baseRequest, id);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @PutMapping(UPDATE_PATH)
    public ResponseEntity<UpdateMerchantResponse> updateMerchant(@Valid @RequestBody UpdateMerchantRequest request) {
        UpdateMerchantResponse response = adminMerchantService.updateMerchant(request);
        return HttpUtils.buildResponse(request, response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteMerchantResponse> deleteMerchant(@PathVariable UUID id, HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        DeleteMerchantResponse response = adminMerchantService.deleteMerchant(baseRequest, id);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @PostMapping
    public ResponseEntity<CreateMerchantResponse> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        CreateMerchantResponse response = adminMerchantService.createMerchant(request);
        return HttpUtils.buildResponse(request, response);
    }

    @GetMapping(APPLICATIONS_PATH)
    public ResponseEntity<GetMerchantApplicationFormsResponse> getMerchantApplicationForms(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMerchantApplicationFormsResponse response = adminMerchantService.getMerchantApplicationForms(
                baseRequest,
                status,
                pageNumber,
                pageSize
        );
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(APPLICATIONS_PATH + "/{id}")
    public ResponseEntity<GetMerchantApplicationFormDetailResponse> getMerchantApplicationFormDetail(
            HttpServletRequest request,
            @PathVariable UUID id) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMerchantApplicationFormDetailResponse response =
                adminMerchantService.getMerchantApplicationFormDetail(baseRequest, id);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @PostMapping(APPLICATIONS_PATH + APPROVE_PATH)
    public ResponseEntity<ReviewMerchantApplicationResponse> approveMerchantApplication(
            @Valid @RequestBody ApproveMerchantApplicationRequest request) {
        ReviewMerchantApplicationResponse response = adminMerchantService.approveMerchantApplication(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PostMapping(APPLICATIONS_PATH + REJECT_PATH)
    public ResponseEntity<ReviewMerchantApplicationResponse> rejectMerchantApplication(
            @Valid @RequestBody RejectMerchantApplicationRequest request) {
        ReviewMerchantApplicationResponse response = adminMerchantService.rejectMerchantApplication(request);
        return HttpUtils.buildResponse(request, response);
    }
}
