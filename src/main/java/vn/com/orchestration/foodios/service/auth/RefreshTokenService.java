package vn.com.orchestration.foodios.service.auth;

import vn.com.orchestration.foodios.entity.auth.RefreshToken;
import vn.com.orchestration.foodios.entity.user.User;

public interface RefreshTokenService {
    RefreshToken saveNew(User user, String refreshToken);
}

