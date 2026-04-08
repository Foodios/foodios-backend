package vn.com.orchestration.foodios.service.promotion.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.promotion.CouponPayload;
import vn.com.orchestration.foodios.dto.promotion.CouponRedemptionPayload;
import vn.com.orchestration.foodios.dto.promotion.CreateCouponRequest;
import vn.com.orchestration.foodios.dto.promotion.CreateCouponResponse;
import vn.com.orchestration.foodios.dto.promotion.DeleteCouponResponse;
import vn.com.orchestration.foodios.dto.promotion.GetCouponRedemptionsResponse;
import vn.com.orchestration.foodios.dto.promotion.GetCouponResponse;
import vn.com.orchestration.foodios.dto.promotion.GetCouponsResponse;
import vn.com.orchestration.foodios.dto.promotion.UpdateCouponRequest;
import vn.com.orchestration.foodios.dto.promotion.UpdateCouponResponse;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.promotion.Coupon;
import vn.com.orchestration.foodios.entity.promotion.CouponRedemption;
import vn.com.orchestration.foodios.entity.promotion.CouponScope;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;
import vn.com.orchestration.foodios.entity.promotion.DiscountType;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.CouponRedemptionRepository;
import vn.com.orchestration.foodios.repository.CouponRepository;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.promotion.MerchantCouponService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class MerchantCouponServiceImpl implements MerchantCouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final UserRepository userRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional
    public CreateCouponResponse createCoupon(CreateCouponRequest request) {
        CreateCouponRequest.CreateCouponRequestData data = requireData(request, request.getData());
        Merchant merchant = resolveMerchant(request, data.getMerchantId());
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchant.getId(), currentUser.getId());

        validateCouponInput(request, data.getDiscountType(), data.getDiscountValue(), data.getMinOrderAmount(),
                data.getMaxDiscountAmount(), data.getStartsAt(), data.getEndsAt(), data.getUsageLimit(), data.getPerUserLimit());

        String code = normalizeCode(data.getCode(), request);
        if (couponRepository.existsByMerchantIdAndCodeIgnoreCase(merchant.getId(), code)) {
            throw businessException(request, DUPLICATE_ERROR, "Coupon code already exists for merchant");
        }

        Coupon coupon = Coupon.builder()
                .merchant(merchant)
                .store(null)
                .scope(CouponScope.MERCHANT)
                .code(code)
                .title(trimToNull(data.getTitle()))
                .description(trimToNull(data.getDescription()))
                .discountType(data.getDiscountType())
                .discountValue(data.getDiscountValue())
                .currency(resolveCurrency(data.getCurrency(), data.getDiscountType()))
                .minOrderAmount(data.getMinOrderAmount())
                .maxDiscountAmount(data.getMaxDiscountAmount())
                .stackable(Boolean.TRUE.equals(data.getStackable()))
                .autoApply(Boolean.TRUE.equals(data.getAutoApply()))
                .startsAt(data.getStartsAt())
                .endsAt(data.getEndsAt())
                .usageLimit(data.getUsageLimit())
                .perUserLimit(data.getPerUserLimit())
                .status(data.getStatus() == null ? CouponStatus.ACTIVE : data.getStatus())
                .build();

        Coupon savedCoupon = couponRepository.saveAndFlush(coupon);
        return CreateCouponResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedCoupon))
                .build();
    }

    @Override
    @Transactional
    public UpdateCouponResponse updateCoupon(UUID couponId, UpdateCouponRequest request) {
        UpdateCouponRequest.UpdateCouponRequestData data = requireData(request, request.getData());
        Merchant merchant = resolveMerchant(request, data.getMerchantId());
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchant.getId(), currentUser.getId());

        Coupon coupon = resolveMerchantCoupon(request, couponId, merchant.getId());

        DiscountType discountType = data.getDiscountType() != null ? data.getDiscountType() : coupon.getDiscountType();
        BigDecimal discountValue = data.getDiscountValue() != null ? data.getDiscountValue() : coupon.getDiscountValue();
        BigDecimal minOrderAmount = data.getMinOrderAmount() != null ? data.getMinOrderAmount() : coupon.getMinOrderAmount();
        BigDecimal maxDiscountAmount = data.getMaxDiscountAmount() != null ? data.getMaxDiscountAmount() : coupon.getMaxDiscountAmount();
        Instant startsAt = data.getStartsAt() != null ? data.getStartsAt() : coupon.getStartsAt();
        Instant endsAt = data.getEndsAt() != null ? data.getEndsAt() : coupon.getEndsAt();
        Integer usageLimit = data.getUsageLimit() != null ? data.getUsageLimit() : coupon.getUsageLimit();
        Integer perUserLimit = data.getPerUserLimit() != null ? data.getPerUserLimit() : coupon.getPerUserLimit();
        validateCouponInput(request, discountType, discountValue, minOrderAmount, maxDiscountAmount, startsAt, endsAt, usageLimit, perUserLimit);

        if (data.getCode() != null) {
            String code = normalizeCode(data.getCode(), request);
            if (couponRepository.existsByMerchantIdAndCodeIgnoreCaseAndIdNot(merchant.getId(), code, coupon.getId())) {
                throw businessException(request, DUPLICATE_ERROR, "Coupon code already exists for merchant");
            }
            coupon.setCode(code);
        }
        if (data.getTitle() != null) {
            coupon.setTitle(trimToNull(data.getTitle()));
        }
        if (data.getDescription() != null) {
            coupon.setDescription(trimToNull(data.getDescription()));
        }
        if (data.getDiscountType() != null) {
            coupon.setDiscountType(data.getDiscountType());
        }
        if (data.getDiscountValue() != null) {
            coupon.setDiscountValue(data.getDiscountValue());
        }
        if (data.getCurrency() != null) {
            coupon.setCurrency(resolveCurrency(data.getCurrency(), discountType));
        }
        if (data.getMinOrderAmount() != null) {
            coupon.setMinOrderAmount(data.getMinOrderAmount());
        }
        if (data.getMaxDiscountAmount() != null) {
            coupon.setMaxDiscountAmount(data.getMaxDiscountAmount());
        }
        if (data.getStackable() != null) {
            coupon.setStackable(data.getStackable());
        }
        if (data.getAutoApply() != null) {
            coupon.setAutoApply(data.getAutoApply());
        }
        if (data.getStartsAt() != null) {
            coupon.setStartsAt(data.getStartsAt());
        }
        if (data.getEndsAt() != null) {
            coupon.setEndsAt(data.getEndsAt());
        }
        if (data.getUsageLimit() != null) {
            coupon.setUsageLimit(data.getUsageLimit());
        }
        if (data.getPerUserLimit() != null) {
            coupon.setPerUserLimit(data.getPerUserLimit());
        }
        if (data.getStatus() != null) {
            coupon.setStatus(data.getStatus());
        }

        Coupon savedCoupon = couponRepository.saveAndFlush(coupon);
        return UpdateCouponResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedCoupon))
                .build();
    }

    @Override
    @Transactional
    public DeleteCouponResponse deleteCoupon(UUID couponId, BaseRequest request, UUID merchantId) {
        Merchant merchant = resolveMerchant(request, merchantId);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchant.getId(), currentUser.getId());

        Coupon coupon = resolveMerchantCoupon(request, couponId, merchant.getId());
        coupon.setStatus(CouponStatus.INACTIVE);
        Coupon savedCoupon = couponRepository.saveAndFlush(coupon);

        return DeleteCouponResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedCoupon))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCouponResponse getCoupon(UUID couponId, BaseRequest request, UUID merchantId) {
        Merchant merchant = resolveMerchant(request, merchantId);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchant.getId(), currentUser.getId());

        Coupon coupon = resolveMerchantCoupon(request, couponId, merchant.getId());
        return GetCouponResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(coupon))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCouponsResponse getCoupons(BaseRequest request, UUID merchantId, CouponStatus status) {
        Merchant merchant = resolveMerchant(request, merchantId);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchant.getId(), currentUser.getId());

        List<Coupon> coupons = (status == null
                ? couponRepository.findByMerchantId(merchant.getId())
                : couponRepository.findByMerchantIdAndStatus(merchant.getId(), status))
                .stream()
                .filter(coupon -> coupon.getScope() == CouponScope.MERCHANT)
                .sorted(Comparator.comparing(Coupon::getCreatedAt).reversed())
                .toList();

        return GetCouponsResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetCouponsResponse.GetCouponsResponseData.builder()
                        .coupons(coupons.stream().map(this::toPayload).toList())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCouponRedemptionsResponse getCouponRedemptions(UUID couponId, BaseRequest request, UUID merchantId) {
        Merchant merchant = resolveMerchant(request, merchantId);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchant.getId(), currentUser.getId());

        Coupon coupon = resolveMerchantCoupon(request, couponId, merchant.getId());
        List<CouponRedemption> redemptions = couponRedemptionRepository.findByCouponId(coupon.getId()).stream()
                .sorted(Comparator.comparing(CouponRedemption::getRedeemedAt).reversed())
                .toList();

        return GetCouponRedemptionsResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetCouponRedemptionsResponse.GetCouponRedemptionsResponseData.builder()
                        .redemptions(redemptions.stream().map(this::toRedemptionPayload).toList())
                        .build())
                .build();
    }

    private <T> T requireData(BaseRequest request, T data) {
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }
        return data;
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

    private Merchant resolveMerchant(BaseRequest request, UUID merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Merchant not found"));
    }

    private void authorizeMerchantAccess(BaseRequest request, UUID merchantId, UUID userId) {
        boolean hasAccess = merchantMemberRepository.existsByMerchantIdAndUserIdAndStatusAndRoleIn(
                merchantId,
                userId,
                MerchantMemberStatus.ACTIVE,
                List.of(MerchantMemberRole.OWNER, MerchantMemberRole.MANAGER)
        );
        if (!hasAccess) {
            throw businessException(request, INVALID_INPUT_ERROR, MERCHANT_ACCESS_DENIED_MESSAGE);
        }
    }

    private Coupon resolveMerchantCoupon(BaseRequest request, UUID couponId, UUID merchantId) {
        return couponRepository.findById(couponId)
                .filter(coupon -> coupon.getMerchant() != null && coupon.getMerchant().getId().equals(merchantId))
                .filter(coupon -> coupon.getScope() == CouponScope.MERCHANT)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Coupon not found"));
    }

    private void validateCouponInput(
            BaseRequest request,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Instant startsAt,
            Instant endsAt,
            Integer usageLimit,
            Integer perUserLimit
    ) {
        if (discountType == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Discount type is required");
        }
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Discount value must be greater than zero");
        }
        if (discountType == DiscountType.PERCENT && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Percent discount cannot be greater than 100");
        }
        if (minOrderAmount != null && minOrderAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Minimum order amount cannot be negative");
        }
        if (maxDiscountAmount != null && maxDiscountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Maximum discount amount must be greater than zero");
        }
        if (startsAt != null && endsAt != null && endsAt.isBefore(startsAt)) {
            throw businessException(request, INVALID_INPUT_ERROR, "Coupon end time must be after start time");
        }
        if (usageLimit != null && usageLimit <= 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Usage limit must be greater than zero");
        }
        if (perUserLimit != null && perUserLimit <= 0) {
            throw businessException(request, INVALID_INPUT_ERROR, "Per-user limit must be greater than zero");
        }
    }

    private String normalizeCode(String code, BaseRequest request) {
        String normalized = trimToNull(code);
        if (normalized == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Coupon code is required");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String resolveCurrency(String currency, DiscountType discountType) {
        if (discountType == DiscountType.PERCENT) {
            return "VND";
        }
        String normalized = trimToNull(currency);
        return normalized == null ? "VND" : normalized.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private CouponPayload toPayload(Coupon coupon) {
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
                .redemptionCount(couponRedemptionRepository.countByCouponId(coupon.getId()))
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    private CouponRedemptionPayload toRedemptionPayload(CouponRedemption redemption) {
        return CouponRedemptionPayload.builder()
                .id(redemption.getId())
                .couponId(redemption.getCoupon().getId())
                .userId(redemption.getUser().getId())
                .orderId(redemption.getOrder() != null ? redemption.getOrder().getId() : null)
                .discountAmount(redemption.getDiscountAmount())
                .redeemedAt(redemption.getRedeemedAt())
                .createdAt(redemption.getCreatedAt())
                .updatedAt(redemption.getUpdatedAt())
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
