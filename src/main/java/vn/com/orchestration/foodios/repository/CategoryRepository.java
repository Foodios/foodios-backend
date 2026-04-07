package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.catalog.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  Optional<Category> findByStoreIdAndSlug(UUID storeId, String slug);

  List<Category> findByStoreIdAndActiveTrueOrderBySortOrderAsc(UUID storeId);

  List<Category> findByStoreId(UUID storeId);
}
