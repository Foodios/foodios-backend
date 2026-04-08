package vn.com.orchestration.foodios.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CreateProductResponse extends BaseResponse<CreateProductResponse.CreateProductResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateProductResponseData {
        private UUID id;
        private UUID storeId;
        private UUID categoryId;
        private String name;
        private String slug;
        private String description;
        private BigDecimal price;
        private String currency;
        private String sku;
        private String imageUrl;
        private int internalStock;
        private ProductStatus status;
    }
}
