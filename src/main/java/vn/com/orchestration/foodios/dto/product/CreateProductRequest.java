package vn.com.orchestration.foodios.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateProductRequest extends BaseRequest {

    @Valid
    @NotNull
    private CreateProductRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateProductRequestData {
        @NotNull
        private UUID storeId;
        private UUID categoryId;
        @NotBlank
        @Size(max = 200)
        private String name;
        @Size(max = 5000)
        private String description;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal price;
        @Size(max = 3)
        private String currency;
        @Size(max = 64)
        private String sku;
        @Size(max = 500)
        private String imageUrl;
        @Min(0)
        private Integer internalStock;
        private ProductStatus status;
    }
}
