package vn.com.orchestration.foodios.dto.merchant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RejectMerchantApplicationRequest extends BaseRequest {

    @Valid
    @NotNull
    private RejectMerchantApplicationRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class RejectMerchantApplicationRequestData {
        @Size(max = 1000)
        @jakarta.validation.constraints.NotBlank
        private String reason;
    }
}
