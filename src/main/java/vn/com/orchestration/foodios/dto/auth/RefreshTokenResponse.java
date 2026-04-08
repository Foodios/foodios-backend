package vn.com.orchestration.foodios.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RefreshTokenResponse extends BaseResponse<RefreshTokenResponse.RefreshTokenResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshTokenResponseData {
        private String accessToken;
        private String refreshToken;
        private UUID userId;
        private OffsetDateTime accessTokenExpiredAt;
        private OffsetDateTime refreshTokenExpiredAt;
    }
}
