package vn.com.orchestration.foodios.service.auth;

import vn.com.orchestration.foodios.entity.auth.RefreshToken;
import vn.com.orchestration.foodios.entity.auth.RefreshTokenStatus;
import vn.com.orchestration.foodios.entity.user.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken saveNew(User user, String refreshToken);

    RefreshToken findByToken(String token);

    RefreshToken updateStatus(RefreshToken refreshToken, RefreshTokenStatus status, OffsetDateTime at);

    void revokeByToken(String token);

    UUID extractUserId(String token);
}
