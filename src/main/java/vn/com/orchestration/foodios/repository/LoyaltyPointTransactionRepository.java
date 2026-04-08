package vn.com.orchestration.foodios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.loyalty.LoyaltyPointTransaction;

import java.util.List;
import java.util.UUID;

public interface LoyaltyPointTransactionRepository extends JpaRepository<LoyaltyPointTransaction, UUID> {
  List<LoyaltyPointTransaction> findByAccountIdOrderByOccurredAtDesc(UUID accountId);

  Page<LoyaltyPointTransaction> findByAccountId(UUID accountId, Pageable pageable);
}

