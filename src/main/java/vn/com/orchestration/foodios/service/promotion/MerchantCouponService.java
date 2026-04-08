package vn.com.orchestration.foodios.service.promotion;

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

import java.util.UUID;

public interface MerchantCouponService {

    CreateCouponResponse createCoupon(CreateCouponRequest request);

    UpdateCouponResponse updateCoupon(UUID couponId, UpdateCouponRequest request);

    DeleteCouponResponse deleteCoupon(UUID couponId, BaseRequest request, UUID merchantId);

    GetCouponResponse getCoupon(UUID couponId, BaseRequest request, UUID merchantId);

    GetCouponsResponse getCoupons(BaseRequest request, UUID merchantId, CouponStatus status);

    GetCouponRedemptionsResponse getCouponRedemptions(UUID couponId, BaseRequest request, UUID merchantId);
}
