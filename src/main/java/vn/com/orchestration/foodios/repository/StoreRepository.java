package vn.com.orchestration.foodios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
  Optional<Store> findBySlug(String slug);

  Page<Store> findByNameContainingIgnoreCase(String name, Pageable pageable);

  List<Store> findTop10ByStatusAndNameContainingIgnoreCaseOrderByNameAsc(
      StoreStatus status, String name);

  List<Store> findByMerchantId(UUID merchantId);

  Page<Store> findByMerchantId(UUID merchantId, Pageable pageable);

  List<Store> findByMerchantIdAndStatus(UUID merchantId, StoreStatus status);

  Page<Store> findByMerchantIdAndStatus(UUID merchantId, StoreStatus status, Pageable pageable);

  boolean existsBySlug(String slug);
}
