package vn.com.orchestration.foodios.service.promotion;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.promotion.*;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;

import java.util.UUID;

public interface AdminPromotionService {
    CreateCouponResponse createGlobalCoupon(AdminCreateCouponRequest request);
    UpdateCouponResponse updateGlobalCoupon(UUID couponId, AdminUpdateCouponRequest request);
    DeleteCouponResponse deleteGlobalCoupon(UUID couponId, BaseRequest request);
    GetCouponsResponse getGlobalCoupons(BaseRequest request, CouponStatus status);
    GetCouponResponse getGlobalCoupon(UUID couponId, BaseRequest request);
}
