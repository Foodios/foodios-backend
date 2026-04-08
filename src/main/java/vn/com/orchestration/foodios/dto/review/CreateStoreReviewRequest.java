package vn.com.orchestration.foodios.dto.review;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateStoreReviewRequest extends BaseRequest {

    @Valid
    @NotNull
    private CreateStoreReviewRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateStoreReviewRequestData {
        @NotNull
        private UUID storeId;
        @NotNull
        @Min(1)
        @Max(5)
        private Integer rating;
        @Size(max = 160)
        private String title;
        @Size(max = 1000)
        private String comment;
    }
}
