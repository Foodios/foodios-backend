package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.merchant.Store;

public interface StoreRepository extends JpaRepository<Store, UUID> {
  Optional<Store> findBySlug(String slug);

  List<Store> findByMerchantId(UUID merchantId);
}

