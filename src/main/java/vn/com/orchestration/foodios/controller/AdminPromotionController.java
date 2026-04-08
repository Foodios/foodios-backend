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
import vn.com.orchestration.foodios.dto.promotion.*;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.promotion.AdminPromotionService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.ADMIN_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.CREATE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.PROMOTIONS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + ADMIN_PATH + PROMOTIONS_PATH)
@RequiredArgsConstructor
public class AdminPromotionController {

    private final AdminPromotionService adminPromotionService;

    @PostMapping(CREATE_PATH)
    public ResponseEntity<CreateCouponResponse> createGlobalCoupon(@Valid @RequestBody AdminCreateCouponRequest request) {
        CreateCouponResponse response = adminPromotionService.createGlobalCoupon(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PutMapping("/{couponId}")
    public ResponseEntity<UpdateCouponResponse> updateGlobalCoupon(
            @PathVariable UUID couponId,
            @Valid @RequestBody AdminUpdateCouponRequest request
    ) {
        UpdateCouponResponse response = adminPromotionService.updateGlobalCoupon(couponId, request);
        return HttpUtils.buildResponse(request, response);
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<DeleteCouponResponse> deleteGlobalCoupon(
            @PathVariable UUID couponId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        DeleteCouponResponse response = adminPromotionService.deleteGlobalCoupon(couponId, baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<GetCouponResponse> getGlobalCoupon(
            @PathVariable UUID couponId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetCouponResponse response = adminPromotionService.getGlobalCoupon(couponId, baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping
    public ResponseEntity<GetCouponsResponse> getGlobalCoupons(
            @RequestParam(required = false) CouponStatus status,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetCouponsResponse response = adminPromotionService.getGlobalCoupons(baseRequest, status);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
