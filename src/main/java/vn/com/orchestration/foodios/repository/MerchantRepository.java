package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.merchant.Merchant;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
  Optional<Merchant> findBySlug(String slug);

  boolean existsBySlug(String slug);

  boolean existsByContactEmailIgnoreCase(String contactEmail);

    Optional<Merchant> findByLegalName(String legalName);

  boolean existsByLegalName(String legalName);

  boolean existsByTaxCode(String taxCode);

  boolean existsByBusinessRegistrationNumber(String registrationNo);
}
