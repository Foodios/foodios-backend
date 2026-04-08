package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.auth.RefreshToken;
import vn.com.orchestration.foodios.entity.auth.RefreshTokenStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser_IdAndStatus(UUID userId, RefreshTokenStatus status);
}
