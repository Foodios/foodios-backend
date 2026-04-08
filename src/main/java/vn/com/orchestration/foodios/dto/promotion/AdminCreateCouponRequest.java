package vn.com.orchestration.foodios.dto.promotion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;
import vn.com.orchestration.foodios.entity.promotion.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AdminCreateCouponRequest extends BaseRequest {

    @Valid
    @NotNull
    private AdminCreateCouponRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminCreateCouponRequestData {
        @NotBlank
        @Size(max = 40)
        private String code;
        @Size(max = 160)
        private String title;
        @Size(max = 500)
        private String description;
        @NotNull
        private DiscountType discountType;
        @NotNull
        @DecimalMin(value = "0.01")
        private BigDecimal discountValue;
        @Size(max = 3)
        private String currency;
        private BigDecimal minOrderAmount;
        private BigDecimal maxDiscountAmount;
        private Boolean stackable;
        private Boolean autoApply;
        private Instant startsAt;
        private Instant endsAt;
        private Integer usageLimit;
        private Integer perUserLimit;
        private CouponStatus status;
    }
}
