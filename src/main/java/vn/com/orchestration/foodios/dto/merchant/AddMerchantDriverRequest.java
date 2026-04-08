package vn.com.orchestration.foodios.dto.merchant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AddMerchantDriverRequest extends BaseRequest {

    @Valid
    @NotNull
    private AddMerchantDriverRequestData data;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class AddMerchantDriverRequestData {
        @NotNull
        private String merchantId;
        @NotNull
        private String userId;
    }
}
