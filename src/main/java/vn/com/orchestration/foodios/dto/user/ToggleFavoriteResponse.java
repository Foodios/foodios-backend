package vn.com.orchestration.foodios.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ToggleFavoriteResponse extends BaseResponse<ToggleFavoriteResponse.ToggleFavoriteResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ToggleFavoriteResponseData {
        private boolean isFavorite;
        private String message;
    }
}
