package vn.com.orchestration.foodios.service.auth.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.entity.auth.RefreshToken;
import vn.com.orchestration.foodios.entity.auth.RefreshTokenStatus;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.jwt.JwtService;
import vn.com.orchestration.foodios.repository.RefreshTokenRepository;
import vn.com.orchestration.foodios.service.auth.RefreshTokenService;

import java.time.OffsetDateTime;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public RefreshToken saveNew(User user, String refreshToken) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User must be persisted");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token must not be blank");
        }

        OffsetDateTime issuedAt = jwtService.extractIssuedAt(refreshToken);
        OffsetDateTime expiredAt = jwtService.extractExpiration(refreshToken);

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .status(RefreshTokenStatus.ACTIVE)
                .issuedAt(issuedAt)
                .expiredAt(expiredAt)
                .build();
        return refreshTokenRepository.saveAndFlush(entity);
    }
}

