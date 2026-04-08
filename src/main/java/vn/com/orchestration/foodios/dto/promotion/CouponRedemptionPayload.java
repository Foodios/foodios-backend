package vn.com.orchestration.foodios.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRedemptionPayload {
    private UUID id;
    private UUID couponId;
    private UUID userId;
    private UUID orderId;
    private BigDecimal discountAmount;
    private Instant redeemedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
