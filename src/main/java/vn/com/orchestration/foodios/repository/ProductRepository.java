package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.catalog.Product;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;

public interface ProductRepository extends JpaRepository<Product, UUID> {
  Optional<Product> findByStoreIdAndSlug(UUID storeId, String slug);

  List<Product> findByStoreId(UUID storeId);

  List<Product> findByStoreIdAndStatus(UUID storeId, ProductStatus status);

  List<Product> findByStoreIdAndStatusOrderBySortOrderAscNameAsc(
      UUID storeId, ProductStatus status);

  Page<Product> findByStoreId(UUID storeId, Pageable pageable);

  Page<Product> findByStoreIdAndStatus(UUID storeId, ProductStatus status, Pageable pageable);

  Page<Product> findByStoreIdAndCategoryId(UUID storeId, UUID categoryId, Pageable pageable);

  Page<Product> findByStoreIdAndCategoryIdAndStatus(
      UUID storeId, UUID categoryId, ProductStatus status, Pageable pageable);

  Page<Product> findByStoreIdAndNameContainingIgnoreCase(
      UUID storeId, String name, Pageable pageable);

  boolean existsByStoreIdAndSlug(UUID storeId, String slug);

  boolean existsByStoreIdAndSku(UUID storeId, String sku);
}
