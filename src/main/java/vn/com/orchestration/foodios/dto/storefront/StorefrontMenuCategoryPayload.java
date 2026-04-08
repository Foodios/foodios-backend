package vn.com.orchestration.foodios.dto.storefront;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorefrontMenuCategoryPayload {
    private UUID categoryId;
    private UUID storeId;
    private String storeName;
    private UUID parentId;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private int sortOrder;
    private List<StorefrontProductPayload> products;
}
