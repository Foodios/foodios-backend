package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetMerchantDetailResponse extends BaseResponse<GetMerchantDetailResponse.GetMerchantDetailResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetMerchantDetailResponseData {
        private UUID id;
        private String displayName;
        private String legalName;
        private String description;
        private String taxCode;
        private String businessRegistrationNumber;
        private String businessLicenseImageUrl;
        private String foodSafetyLicenseImageUrl;
        private String slug;
        private String logoUrl;
        private String cuisineCategory;
        private String contactEmail;
        private String supportHotline;
        private String status;
        private BigDecimal commissionRate;
        private Double rating;
        private Long totalReviews;
        private Long totalOrders;
        private BigDecimal mtdRevenue;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }
}
