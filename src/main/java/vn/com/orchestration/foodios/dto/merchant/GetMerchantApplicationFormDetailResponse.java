package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetMerchantApplicationFormDetailResponse
        extends BaseResponse<GetMerchantApplicationFormDetailResponse.GetMerchantApplicationFormDetailResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class GetMerchantApplicationFormDetailResponseData {
        private UUID id;
        private String formCode;
        private String merchantId;
        private String submittedBy;
        private String legalName;
        private String status;
        private OffsetDateTime submittedAt;
        private OffsetDateTime approvedAt;
        private String approvedBy;
        private String rejectedBy;
        private String rejectionReason;
        private String ownerFullName;
        private String ownerEmail;
        private String ownerPhone;
        private String merchantName;
        private String displayName;
        private String description;
        private String taxCode;
        private String businessRegistrationNumber;
        private String businessLicenseImageUrl;
        private String foodSafetyLicenseImageUrl;
        private String contactPhone;
        private String contactEmail;
        private String contactName;
        private String line1;
        private String line2;
        private String district;
        private String city;
        private String province;
        private String postalCode;
        private String country;
        private String bankName;
        private String bankAccountName;
        private String bankAccountNumber;
        private String bankBranch;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }
}
