package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.promotion.Coupon;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
  Optional<Coupon> findByCodeIgnoreCase(String code);

  Optional<Coupon> findByStoreIdAndCodeIgnoreCase(UUID storeId, String code);

  List<Coupon> findByStoreId(UUID storeId);

  List<Coupon> findByStoreIdAndStatus(UUID storeId, CouponStatus status);

  List<Coupon> findByMerchantId(UUID merchantId);

  Page<Coupon> findByStoreId(UUID storeId, Pageable pageable);

  Page<Coupon> findByStoreIdAndStatus(UUID storeId, CouponStatus status, Pageable pageable);

  Page<Coupon> findByMerchantId(UUID merchantId, Pageable pageable);

  Page<Coupon> findByMerchantIdAndStatus(UUID merchantId, CouponStatus status, Pageable pageable);

  boolean existsByStoreIdAndCodeIgnoreCase(UUID storeId, String code);

  boolean existsByMerchantIdAndCodeIgnoreCase(UUID merchantId, String code);
}
