package vn.com.orchestration.foodios.dto.user;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UpdateProfileRequest extends BaseRequest {
    @Valid
    @NotNull
    private UpdateProfileRequestData data;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class UpdateProfileRequestData {
        private String userId;
        private String fullName;
        private String phone;
        @Email
        private String email;
        private String avatarUrl;
    }
}
