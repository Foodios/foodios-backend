package vn.com.orchestration.foodios.dto.merchant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ApproveMerchantApplicationRequest extends BaseRequest {

    @Valid
    @NotNull
    private ApproveMerchantApplicationRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class ApproveMerchantApplicationRequestData {
        @NotNull
        private UUID id;
    }
}
