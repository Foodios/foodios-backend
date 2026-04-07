package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.catalog.Product;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, UUID> {
  Optional<Product> findByStoreIdAndSlug(UUID storeId, String slug);

  List<Product> findByStoreId(UUID storeId);

  List<Product> findByStoreIdAndStatus(UUID storeId, ProductStatus status);
}

