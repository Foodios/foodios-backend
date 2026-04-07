package vn.com.orchestration.foodios.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.loyalty.LoyaltyAccount;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {
  Optional<LoyaltyAccount> findByUserId(UUID userId);
}

