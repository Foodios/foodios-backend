package vn.com.orchestration.foodios.dto.category;

import jakarta.validation.Valid;
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
import vn.com.orchestration.foodios.entity.catalog.CategoryStatus;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateCategoryRequest extends BaseRequest {

    @Valid
    @NotNull
    private CreateCategoryRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCategoryRequestData {
        @NotNull
        private UUID storeId;
        private UUID parentId;
        @NotBlank
        @Size(max = 160)
        private String name;
        @Size(max = 180)
        private String slug;
        @Size(max = 500)
        private String description;
        @Size(max = 500)
        private String imageUrl;
        @Min(0)
        private Integer sortOrder;
        private CategoryStatus status;
        private Boolean active;
    }
}
