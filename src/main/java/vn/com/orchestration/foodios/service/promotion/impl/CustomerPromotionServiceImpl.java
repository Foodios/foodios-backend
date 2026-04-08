package vn.com.orchestration.foodios.service.promotion.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.promotion.ValidatePromotionResponse;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.entity.promotion.Coupon;
import vn.com.orchestration.foodios.entity.promotion.CouponScope;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;
import vn.com.orchestration.foodios.entity.promotion.DiscountType;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.CouponRedemptionRepository;
import vn.com.orchestration.foodios.repository.CouponRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.promotion.CustomerPromotionService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class CustomerPromotionServiceImpl implements CustomerPromotionService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional(readOnly = true)
    public ValidatePromotionResponse validatePromotion(BaseRequest request, String code, UUID storeId, BigDecimal orderAmount) {
        String normalizedCode = trimToNull(code);
        if (normalizedCode == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Promo code is required");
        }
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Order amount must be zero or greater");
        }

        User currentUser = resolveCurrentUser(request);
        Store store = storeRepository.findById(storeId)
                .filter(item -> item.getStatus() == StoreStatus.ACTIVE)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));

        Coupon coupon = couponRepository.findByMerchantId(store.getMerchant().getId()).stream()
                .filter(item -> item.getCode().equalsIgnoreCase(normalizedCode))
                .filter(item -> item.getStatus() == CouponStatus.ACTIVE)
                .filter(item -> item.getScope() == CouponScope.MERCHANT
                        || (item.getScope() == CouponScope.STORE
                        && item.getStore() != null
                        && item.getStore().getId().equals(store.getId())))
                .findFirst()
                .orElseThrow(() -> businessException(request, INVALID_INPUT_ERROR, "Promo code is invalid"));

        validateCouponApplicability(request, coupon, orderAmount, currentUser.getId());

        long totalRedemptions = couponRedemptionRepository.countByCouponId(coupon.getId());
        long currentUserRedemptions = couponRedemptionRepository.countByCouponIdAndUserId(coupon.getId(), currentUser.getId());
        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        return ValidatePromotionResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(ValidatePromotionResponse.ValidatePromotionResponseData.builder()
                        .valid(true)
                        .couponId(coupon.getId())
                        .merchantId(coupon.getMerchant() != null ? coupon.getMerchant().getId() : null)
                        .storeId(store.getId())
                        .code(coupon.getCode())
                        .title(coupon.getTitle())
                        .description(coupon.getDescription())
                        .scope(coupon.getScope())
                        .discountType(coupon.getDiscountType())
                        .discountValue(coupon.getDiscountValue())
                        .discountAmount(discountAmount)
                        .orderAmount(orderAmount)
                        .finalAmount(finalAmount)
                        .currency(coupon.getCurrency())
                        .minOrderAmount(coupon.getMinOrderAmount())
                        .maxDiscountAmount(coupon.getMaxDiscountAmount())
                        .usageLimit(coupon.getUsageLimit())
                        .perUserLimit(coupon.getPerUserLimit())
                        .totalRedemptions(totalRedemptions)
                        .currentUserRedemptions(currentUserRedemptions)
                        .startsAt(coupon.getStartsAt())
                        .endsAt(coupon.getEndsAt())
                        .build())
                .build();
    }

    private void validateCouponApplicability(BaseRequest request, Coupon coupon, BigDecimal orderAmount, UUID userId) {
        Instant now = Instant.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Promo code is not active yet");
        }
        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Promo code has expired");
        }
        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Order does not meet minimum amount for promo code");
        }
        if (coupon.getUsageLimit() != null && couponRedemptionRepository.countByCouponId(coupon.getId()) >= coupon.getUsageLimit()) {
            throw businessException(request, INVALID_INPUT_ERROR, "Promo code usage limit reached");
        }
        if (coupon.getPerUserLimit() != null
                && couponRedemptionRepository.countByCouponIdAndUserId(coupon.getId(), userId) >= coupon.getPerUserLimit()) {
            throw businessException(request, INVALID_INPUT_ERROR, "Promo code per-user limit reached");
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount = coupon.getDiscountType() == DiscountType.PERCENT
                ? orderAmount.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountValue();
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }
        return discount.min(orderAmount).max(BigDecimal.ZERO);
    }

    private User resolveCurrentUser(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isBlank()) {
            return userRepository.findByEmail(currentUser.email())
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        }
        try {
            UUID userId = UUID.fromString(currentUser.subject());
            return userRepository.findById(userId)
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        } catch (IllegalArgumentException exception) {
            throw businessException(request, INVALID_INPUT_ERROR, "Invalid current user");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
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
