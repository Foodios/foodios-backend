package vn.com.orchestration.foodios.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.entity.promotion.CouponScope;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;
import vn.com.orchestration.foodios.entity.promotion.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponPayload {
    private UUID id;
    private UUID merchantId;
    private String code;
    private String title;
    private String description;
    private CouponScope scope;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private String currency;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private boolean stackable;
    private boolean autoApply;
    private Instant startsAt;
    private Instant endsAt;
    private Integer usageLimit;
    private Integer perUserLimit;
    private CouponStatus status;
    private long redemptionCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
