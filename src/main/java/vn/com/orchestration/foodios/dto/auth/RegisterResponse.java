package vn.com.orchestration.foodios.dto.auth;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.entity.user.UserStatus;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RegisterResponse extends BaseResponse<RegisterResponse.RegisterResponseData> {

  @Getter
  @Setter
  @NoArgsConstructor
  @Builder
  @AllArgsConstructor
  public static class RegisterResponseData {
    private UUID id;
    private String username;
    private String email;
    private String phone;
    private UserStatus status;
  }

}
