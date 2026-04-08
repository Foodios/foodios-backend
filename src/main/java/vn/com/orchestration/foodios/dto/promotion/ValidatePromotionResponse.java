package vn.com.orchestration.foodios.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.entity.promotion.CouponScope;
import vn.com.orchestration.foodios.entity.promotion.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ValidatePromotionResponse extends BaseResponse<ValidatePromotionResponse.ValidatePromotionResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidatePromotionResponseData {
        private Boolean valid;
        private UUID couponId;
        private UUID merchantId;
        private UUID storeId;
        private String code;
        private String title;
        private String description;
        private CouponScope scope;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private BigDecimal discountAmount;
        private BigDecimal orderAmount;
        private BigDecimal finalAmount;
        private String currency;
        private BigDecimal minOrderAmount;
        private BigDecimal maxDiscountAmount;
        private Integer usageLimit;
        private Integer perUserLimit;
        private Long totalRedemptions;
        private Long currentUserRedemptions;
        private Instant startsAt;
        private Instant endsAt;
    }
}
