package vn.com.orchestration.foodios.service.promotion.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.promotion.*;
import vn.com.orchestration.foodios.entity.promotion.Coupon;
import vn.com.orchestration.foodios.entity.promotion.CouponScope;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.CouponRepository;
import vn.com.orchestration.foodios.service.promotion.AdminPromotionService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.*;

@Service
@RequiredArgsConstructor
public class AdminPromotionServiceImpl implements AdminPromotionService {

    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_SUPER_ADMIN", "ROLE_PLATFORM_ADMIN");

    private final CouponRepository couponRepository;
    private final IdentityUserContextProvider identityUserContextProvider;

    @Override
    @Transactional
    public CreateCouponResponse createGlobalCoupon(AdminCreateCouponRequest request) {
        authorizeAdmin(request);
        var data = request.getData();

        if (couponRepository.existsByScopeAndCodeIgnoreCase(CouponScope.GLOBAL, data.getCode())) {
            throw businessException(request, DUPLICATE_ERROR, "Global coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(data.getCode().toUpperCase())
                .title(data.getTitle())
                .description(data.getDescription())
                .scope(CouponScope.GLOBAL)
                .discountType(data.getDiscountType())
                .discountValue(data.getDiscountValue())
                .currency(data.getCurrency() != null ? data.getCurrency() : "VND")
                .minOrderAmount(data.getMinOrderAmount())
                .maxDiscountAmount(data.getMaxDiscountAmount())
                .stackable(Boolean.TRUE.equals(data.getStackable()))
                .autoApply(Boolean.TRUE.equals(data.getAutoApply()))
                .startsAt(data.getStartsAt())
                .endsAt(data.getEndsAt())
                .usageLimit(data.getUsageLimit())
                .perUserLimit(data.getPerUserLimit())
                .status(data.getStatus() != null ? data.getStatus() : CouponStatus.ACTIVE)
                .build();

        Coupon saved = couponRepository.save(coupon);

        return CreateCouponResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(mapToPayload(saved))
                .build();
    }

    @Override
    @Transactional
    public UpdateCouponResponse updateGlobalCoupon(UUID couponId, AdminUpdateCouponRequest request) {
        authorizeAdmin(request);
        var data = request.getData();
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Coupon not found"));

        if (coupon.getScope() != CouponScope.GLOBAL) {
            throw businessException(request, INVALID_INPUT_ERROR, "Coupon is not a global coupon");
        }

        if (data.getCode() != null) {
            String newCode = data.getCode().toUpperCase();
            if (!newCode.equals(coupon.getCode()) && couponRepository.existsByScopeAndCodeIgnoreCase(CouponScope.GLOBAL, newCode)) {
                throw businessException(request, DUPLICATE_ERROR, "Global coupon code already exists");
            }
            coupon.setCode(newCode);
        }

        if (data.getTitle() != null) coupon.setTitle(data.getTitle());
        if (data.getDescription() != null) coupon.setDescription(data.getDescription());
        if (data.getDiscountType() != null) coupon.setDiscountType(data.getDiscountType());
        if (data.getDiscountValue() != null) coupon.setDiscountValue(data.getDiscountValue());
        if (data.getCurrency() != null) coupon.setCurrency(data.getCurrency());
        if (data.getMinOrderAmount() != null) coupon.setMinOrderAmount(data.getMinOrderAmount());
        if (data.getMaxDiscountAmount() != null) coupon.setMaxDiscountAmount(data.getMaxDiscountAmount());
        if (data.getStackable() != null) coupon.setStackable(data.getStackable());
        if (data.getAutoApply() != null) coupon.setAutoApply(data.getAutoApply());
        if (data.getStartsAt() != null) coupon.setStartsAt(data.getStartsAt());
        if (data.getEndsAt() != null) coupon.setEndsAt(data.getEndsAt());
        if (data.getUsageLimit() != null) coupon.setUsageLimit(data.getUsageLimit());
        if (data.getPerUserLimit() != null) coupon.setPerUserLimit(data.getPerUserLimit());
        if (data.getStatus() != null) coupon.setStatus(data.getStatus());

        couponRepository.save(coupon);

        return UpdateCouponResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(mapToPayload(coupon))
                .build();
    }

    @Override
    @Transactional
    public DeleteCouponResponse deleteGlobalCoupon(UUID couponId, BaseRequest request) {
        authorizeAdmin(request);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Coupon not found"));

        if (coupon.getScope() != CouponScope.GLOBAL) {
            throw businessException(request, INVALID_INPUT_ERROR, "Coupon is not a global coupon");
        }

        couponRepository.delete(coupon);

        return DeleteCouponResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(mapToPayload(coupon))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCouponsResponse getGlobalCoupons(BaseRequest request, CouponStatus status) {
        authorizeAdmin(request);
        List<Coupon> coupons = status != null 
                ? couponRepository.findByScopeAndStatus(CouponScope.GLOBAL, status)
                : couponRepository.findByScope(CouponScope.GLOBAL);

        return GetCouponsResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetCouponsResponse.GetCouponsResponseData.builder()
                        .coupons(coupons.stream().map(this::mapToPayload).toList())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCouponResponse getGlobalCoupon(UUID couponId, BaseRequest request) {
        authorizeAdmin(request);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Coupon not found"));

        if (coupon.getScope() != CouponScope.GLOBAL) {
            throw businessException(request, INVALID_INPUT_ERROR, "Coupon is not a global coupon");
        }

        return GetCouponResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(mapToPayload(coupon))
                .build();
    }

    private void authorizeAdmin(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        Set<String> roles = currentUser.roles();
        if (roles == null || roles.stream().noneMatch(ADMIN_ROLES::contains)) {
            throw businessException(request, INVALID_INPUT_ERROR, ADMIN_ACCESS_DENIED_MESSAGE);
        }
    }

    private CouponPayload mapToPayload(Coupon coupon) {
        return CouponPayload.builder()
                .id(coupon.getId())
                .merchantId(coupon.getMerchant() != null ? coupon.getMerchant().getId() : null)
                .code(coupon.getCode())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .scope(coupon.getScope())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .currency(coupon.getCurrency())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .stackable(coupon.isStackable())
                .autoApply(coupon.isAutoApply())
                .startsAt(coupon.getStartsAt())
                .endsAt(coupon.getEndsAt())
                .usageLimit(coupon.getUsageLimit())
                .perUserLimit(coupon.getPerUserLimit())
                .status(coupon.getStatus())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }
}
