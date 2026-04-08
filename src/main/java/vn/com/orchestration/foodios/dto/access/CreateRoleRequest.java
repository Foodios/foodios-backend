package vn.com.orchestration.foodios.dto.access;

import jakarta.validation.Valid;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreateRoleRequest extends BaseRequest {

    @Valid
    @NotNull
    private CreateRoleRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRoleRequestData {
        @NotBlank
        @Size(max = 64)
        private String code;
        @NotBlank
        @Size(max = 120)
        private String name;
        @Size(max = 255)
        private String description;
        private Boolean enabled;
    }
}
