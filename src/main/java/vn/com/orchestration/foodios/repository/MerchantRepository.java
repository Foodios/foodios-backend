package vn.com.orchestration.foodios.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.merchant.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
  Optional<Merchant> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsByContactEmailIgnoreCase(String contactEmail);
}
