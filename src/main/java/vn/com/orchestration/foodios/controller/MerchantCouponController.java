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
import vn.com.orchestration.foodios.dto.promotion.CreateCouponRequest;
import vn.com.orchestration.foodios.dto.promotion.CreateCouponResponse;
import vn.com.orchestration.foodios.dto.promotion.DeleteCouponResponse;
import vn.com.orchestration.foodios.dto.promotion.GetCouponRedemptionsResponse;
import vn.com.orchestration.foodios.dto.promotion.GetCouponResponse;
import vn.com.orchestration.foodios.dto.promotion.GetCouponsResponse;
import vn.com.orchestration.foodios.dto.promotion.UpdateCouponRequest;
import vn.com.orchestration.foodios.dto.promotion.UpdateCouponResponse;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.promotion.MerchantCouponService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.COUPONS_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.CREATE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANT_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.REDEMPTIONS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MERCHANT_PATH)
@RequiredArgsConstructor
public class MerchantCouponController {

    private final MerchantCouponService merchantCouponService;

    @PostMapping(COUPONS_PATH + CREATE_PATH)
    public ResponseEntity<CreateCouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CreateCouponResponse response = merchantCouponService.createCoupon(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PutMapping(COUPONS_PATH + "/{couponId}")
    public ResponseEntity<UpdateCouponResponse> updateCoupon(
            @PathVariable UUID couponId,
            @Valid @RequestBody UpdateCouponRequest request
    ) {
        UpdateCouponResponse response = merchantCouponService.updateCoupon(couponId, request);
        return HttpUtils.buildResponse(request, response);
    }

    @DeleteMapping(COUPONS_PATH + "/{couponId}")
    public ResponseEntity<DeleteCouponResponse> deleteCoupon(
            @PathVariable UUID couponId,
            @RequestParam UUID merchantId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        DeleteCouponResponse response = merchantCouponService.deleteCoupon(couponId, baseRequest, merchantId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(COUPONS_PATH + "/{couponId}")
    public ResponseEntity<GetCouponResponse> getCoupon(
            @PathVariable UUID couponId,
            @RequestParam UUID merchantId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetCouponResponse response = merchantCouponService.getCoupon(couponId, baseRequest, merchantId);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(COUPONS_PATH)
    public ResponseEntity<GetCouponsResponse> getCoupons(
            @RequestParam UUID merchantId,
            @RequestParam(required = false) CouponStatus status,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetCouponsResponse response = merchantCouponService.getCoupons(baseRequest, merchantId, status);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(COUPONS_PATH + "/{couponId}" + REDEMPTIONS_PATH)
    public ResponseEntity<GetCouponRedemptionsResponse> getCouponRedemptions(
            @PathVariable UUID couponId,
            @RequestParam UUID merchantId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetCouponRedemptionsResponse response = merchantCouponService.getCouponRedemptions(couponId, baseRequest, merchantId);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
