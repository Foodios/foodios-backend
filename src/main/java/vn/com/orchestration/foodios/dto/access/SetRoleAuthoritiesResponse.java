package vn.com.orchestration.foodios.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SetRoleAuthoritiesResponse extends BaseResponse<SetRoleAuthoritiesResponse.SetRoleAuthoritiesResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SetRoleAuthoritiesResponseData {
        private String roleCode;
        private List<String> authorityCodes;
        private boolean updated;
    }
}
