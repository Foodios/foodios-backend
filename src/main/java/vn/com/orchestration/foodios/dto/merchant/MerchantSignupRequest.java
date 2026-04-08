package vn.com.orchestration.foodios.dto.merchant;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.address.MerchantAddress;
import vn.com.orchestration.foodios.entity.merchant.MerchantPayout;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MerchantSignupRequest extends BaseRequest {

    @Valid
    @NotNull
    private MerchantSignupRequestData data;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class MerchantSignupRequestData {
        private String userId;
        private MerchantOwnerInfo owner;
        private MerchantInfo merchant;
        private MerchantAddressInfo address;
        private MerchantPayoutInfo payout;
        private MerchantOperationHours operatingHours;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class MerchantOwnerInfo {
        @NotBlank
        @NotNull
        private String fullName;
        @Email
        @NotBlank
        @NotNull
        private String email;
        @NotBlank
        @NotNull
        private String phone;
        @NotBlank
        @NotNull
        private String password;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class MerchantInfo {
        private String legalName;
        private String displayName;
        private String description;
        private String taxCode;
        private String businessRegistrationNumber;
        private String businessLicenseImageUrl;
        private String foodSafetyLicenseImageUrl;
        private String contactPhone;
        private String contactEmail;
        private String contactName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class MerchantAddressInfo {
        private String line1;
        private String line2;
        private String district;
        private String city;
        private String province;
        private String postalCode;
        private String country;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class MerchantPayoutInfo {
        private String bankName;
        private String bankAccountName;
        private String bankAccountNumber;
        private String bankBranch;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class MerchantOperationHours {
        private String dayOfWeek;
        private String openTime;
        private String closeTime;
    }
}
