package vn.com.orchestration.foodios.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.auth.OtpPurpose;
import vn.com.orchestration.foodios.entity.auth.UserOtp;

public interface UserOtpRepository extends JpaRepository<UserOtp, UUID> {

  Optional<UserOtp>
      findFirstByUserIdAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
          UUID userId, OtpPurpose purpose, Instant now);
}

