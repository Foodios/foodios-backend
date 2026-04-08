package vn.com.orchestration.foodios.dto.merchant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateMerchantRequest extends BaseRequest {

    @Valid
    @NotNull
    private CreateMerchantRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateMerchantRequestData {
        @NotBlank
        @Size(max = 160)
        private String merchantName;
        @Size(max = 200)
        private String legalName;
        @Size(max = 500)
        private String logoUrl;
        @NotBlank
        @Size(max = 120)
        private String locationDistrict;
        @Size(max = 120)
        private String cuisineCategory;
        @NotBlank
        @Email
        @Size(max = 254)
        private String contactEmail;
        @NotBlank
        @Size(max = 32)
        private String supportHotline;
        @NotBlank
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$")
        private String openingTime;
        @NotBlank
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$")
        private String closingTime;
        private Boolean autoVerify;
    }
}
