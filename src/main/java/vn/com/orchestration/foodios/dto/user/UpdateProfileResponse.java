package vn.com.orchestration.foodios.dto.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UpdateProfileResponse extends BaseResponse<UpdateProfileResponse.UpdateProfileResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @AllArgsConstructor
    public static class UpdateProfileResponseData {
        private String userId;
        private String fullName;
        private String phone;
        private String email;
        private String avatarUrl;
    }
}
