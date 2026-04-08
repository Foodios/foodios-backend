package vn.com.orchestration.foodios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.review.Review;
import vn.com.orchestration.foodios.entity.review.ReviewSourceType;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
  boolean existsByOrderId(UUID orderId);

  Page<Review> findByStoreMerchantId(UUID merchantId, Pageable pageable);

  Page<Review> findByStoreMerchantIdAndStoreId(UUID merchantId, UUID storeId, Pageable pageable);

  Page<Review> findByStoreMerchantIdAndStatus(
      UUID merchantId, ReviewStatus status, Pageable pageable);

  Page<Review> findByStoreMerchantIdAndSourceType(
      UUID merchantId, ReviewSourceType sourceType, Pageable pageable);

  Page<Review> findByStoreMerchantIdAndStoreIdAndStatus(
      UUID merchantId, UUID storeId, ReviewStatus status, Pageable pageable);

  Page<Review> findByStoreMerchantIdAndStoreIdAndSourceType(
      UUID merchantId, UUID storeId, ReviewSourceType sourceType, Pageable pageable);

  Page<Review> findByStoreMerchantIdAndStatusAndSourceType(
      UUID merchantId, ReviewStatus status, ReviewSourceType sourceType, Pageable pageable);

  Page<Review> findByStoreMerchantIdAndStoreIdAndStatusAndSourceType(
      UUID merchantId,
      UUID storeId,
      ReviewStatus status,
      ReviewSourceType sourceType,
      Pageable pageable);

  List<Review> findByStoreMerchantIdAndStatus(UUID merchantId, ReviewStatus status);

  List<Review> findByStoreMerchantIdAndStoreIdAndStatus(
      UUID merchantId, UUID storeId, ReviewStatus status);
}
