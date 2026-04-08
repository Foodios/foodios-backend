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
public class SetUserRoleResponse extends BaseResponse<SetUserRoleResponse.SetUserRoleResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SetUserRoleResponseData {
        private UUID userId;
        private String roleCode;
        private boolean updated;
    }
}
