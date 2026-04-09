package vn.com.orchestration.foodios.dto.wallet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class TopUpRequest extends BaseRequest {

    @Valid
    @NotNull
    private TopUpData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopUpData {
        @NotNull
        @DecimalMin(value = "1000.00", message = "Minimum top-up amount is 1000 VND")
        private BigDecimal amount;
        
        private String description;
    }
}
