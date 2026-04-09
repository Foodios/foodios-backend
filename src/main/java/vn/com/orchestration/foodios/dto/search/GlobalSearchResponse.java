package vn.com.orchestration.foodios.dto.search;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GlobalSearchResponse extends BaseResponse<GlobalSearchResponse.GlobalSearchData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonPropertyOrder({"stores", "products"})
    public static class GlobalSearchData {
        private List<StoreSearchResult> stores;
        private List<ProductSearchResult> products;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonPropertyOrder({"id", "name", "description", "slug", "logoUrl", "rating", "totalReviews"})
    public static class StoreSearchResult {
        private UUID id;
        private String name;
        private String description;
        private String slug;
        private String logoUrl;
        private Double rating;
        private Integer totalReviews;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonPropertyOrder({"id", "name", "description", "slug", "price", "imageUrl", "storeId", "storeName"})
    public static class ProductSearchResult {
        private UUID id;
        private String name;
        private String description;
        private String slug;
        private BigDecimal price;
        private String imageUrl;
        private UUID storeId;
        private String storeName;
    }
}
