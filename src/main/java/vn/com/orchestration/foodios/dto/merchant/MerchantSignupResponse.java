package vn.com.orchestration.foodios.dto.merchant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class MerchantSignupResponse extends BaseResponse<MerchantSignupResponse.MerchantSignupResponseData> {
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @AllArgsConstructor
    public static class MerchantSignupResponseData {
        private String registrationNumber;
        private String status;
        private OffsetDateTime appliedAt;
    }
}
