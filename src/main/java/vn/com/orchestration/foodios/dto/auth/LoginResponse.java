package vn.com.orchestration.foodios.dto.auth;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class LoginResponse extends BaseResponse<LoginResponse.LoginResponseData> {

  @Getter
  @Setter
  @NoArgsConstructor
  @Builder
  @AllArgsConstructor
  public static class LoginResponseData {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String email;
    private Set<String> roles;
    private Set<String> authorities;
    private boolean profileCompleted;
  }
}
