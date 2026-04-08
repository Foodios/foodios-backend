package vn.com.orchestration.foodios.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.time.OffsetDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private OffsetDateTime accessTokenExpiredAt;
    private OffsetDateTime refreshTokenExpiredAt;
    private UUID userId;
    private String email;
    private Set<String> roles;
    private Set<String> authorities;
    private List<UserMerchantMembership> merchantMemberships;
    private boolean profileCompleted;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @Builder
  @AllArgsConstructor
  public static class UserMerchantMembership {
    private UUID merchantId;
    private String merchantName;
    private String merchantSlug;
    private String memberRole;
    private String memberStatus;
    private Instant assignedAt;
  }
}
