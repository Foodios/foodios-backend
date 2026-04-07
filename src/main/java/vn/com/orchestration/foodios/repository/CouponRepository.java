package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.promotion.Coupon;
import vn.com.orchestration.foodios.entity.promotion.CouponStatus;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
  Optional<Coupon> findByCodeIgnoreCase(String code);

  Optional<Coupon> findByStoreIdAndCodeIgnoreCase(UUID storeId, String code);

  List<Coupon> findByStoreId(UUID storeId);

  List<Coupon> findByStoreIdAndStatus(UUID storeId, CouponStatus status);

  List<Coupon> findByMerchantId(UUID merchantId);
}
