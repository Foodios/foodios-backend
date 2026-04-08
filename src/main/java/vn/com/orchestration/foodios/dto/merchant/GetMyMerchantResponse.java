package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.entity.merchant.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetMyMerchantResponse extends BaseResponse<GetMyMerchantResponse.GetMyMerchantResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class GetMyMerchantResponseData {
        private UUID merchantId;
        private String merchantName;
        private String legalName;
        private String merchantSlug;
        private String description;
        private String taxCode;
        private String businessRegistrationNumber;
        private String businessLicenseImageUrl;
        private String foodSafetyLicenseImageUrl;
        private String logoUrl;
        private String cuisineCategory;
        private String contactEmail;
        private String supportHotline;
        private MerchantStatus merchantStatus;
        private String memberRole;
        private String memberStatus;
        private Instant assignedAt;
        private MerchantPayoutInfo payout;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class MerchantPayoutInfo {
        private String bankName;
        private String bankAccountName;
        private String bankAccountNumber;
        private String bankBranch;
    }
}
