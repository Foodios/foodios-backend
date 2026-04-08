package vn.com.orchestration.foodios.dto.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AddCartItemRequest extends BaseRequest {

    @Valid
    @NotNull
    private AddCartItemRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddCartItemRequestData {
        @NotNull
        private UUID storeId;
        @NotNull
        private UUID productId;
        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
