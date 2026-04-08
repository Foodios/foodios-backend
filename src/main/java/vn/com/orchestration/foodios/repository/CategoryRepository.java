package vn.com.orchestration.foodios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.catalog.Category;
import vn.com.orchestration.foodios.entity.catalog.CategoryStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  Optional<Category> findByStoreIdAndSlug(UUID storeId, String slug);

  List<Category> findByStoreIdAndActiveTrueOrderBySortOrderAsc(UUID storeId);

  List<Category> findByStoreIdAndStatusOrderBySortOrderAsc(UUID storeId, CategoryStatus status);

  List<Category> findByStoreMerchantIdAndStatusAndActiveTrue(UUID merchantId, CategoryStatus status);

  List<Category> findByStoreId(UUID storeId);

  Page<Category> findByStoreId(UUID storeId, Pageable pageable);

  Page<Category> findByStoreIdAndStatus(UUID storeId, CategoryStatus status, Pageable pageable);

  boolean existsByStoreIdAndSlug(UUID storeId, String slug);

  boolean existsByStoreIdAndSlugAndIdNot(UUID storeId, String slug, UUID id);

  boolean existsByParentId(UUID parentId);
}
