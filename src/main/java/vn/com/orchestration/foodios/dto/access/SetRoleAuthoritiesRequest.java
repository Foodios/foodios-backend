package vn.com.orchestration.foodios.dto.access;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SetRoleAuthoritiesRequest extends BaseRequest {

    @Valid
    @NotNull
    private SetRoleAuthoritiesRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SetRoleAuthoritiesRequestData {
        @NotBlank
        private String roleCode;
        @NotEmpty
        private List<String> authorityCodes;
    }
}
