package vn.com.orchestration.foodios.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CreateAuthorityResponse extends BaseResponse<CreateAuthorityResponse.CreateAuthorityResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateAuthorityResponseData {
        private UUID id;
        private String code;
        private String name;
        private String description;
        private boolean enabled;
    }
}
