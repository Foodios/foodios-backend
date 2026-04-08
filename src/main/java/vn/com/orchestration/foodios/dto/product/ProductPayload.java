package vn.com.orchestration.foodios.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPayload {
    private UUID id;
    private UUID storeId;
    private UUID categoryId;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String currency;
    private String sku;
    private String imageUrl;
    private int internalStock;
    private int sortOrder;
    private boolean featured;
    private boolean available;
    private Integer preparationTimeMinutes;
    private ProductStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
