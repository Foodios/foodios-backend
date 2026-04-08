package vn.com.orchestration.foodios.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.entity.catalog.CategoryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPayload {
    private UUID id;
    private UUID storeId;
    private UUID parentId;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private int sortOrder;
    private CategoryStatus status;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
