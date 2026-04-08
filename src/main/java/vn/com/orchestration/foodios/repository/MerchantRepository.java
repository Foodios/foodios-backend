package vn.com.orchestration.foodios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.merchant.MerchantStatus;
import vn.com.orchestration.foodios.entity.merchant.Merchant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Page<Merchant> findByDisplayNameContainingIgnoreCase(String displayName, Pageable pageable);

    Optional<Merchant> findBySlug(String slug);

  List<Merchant> findByStatus(MerchantStatus status);

  boolean existsBySlug(String slug);

  boolean existsByContactEmailIgnoreCase(String contactEmail);

    Optional<Merchant> findByLegalName(String legalName);

  boolean existsByLegalName(String legalName);

  boolean existsByTaxCode(String taxCode);

  boolean existsByBusinessRegistrationNumber(String registrationNo);
}
