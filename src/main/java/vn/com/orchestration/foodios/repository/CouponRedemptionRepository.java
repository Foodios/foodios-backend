package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.promotion.CouponRedemption;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, UUID> {
  List<CouponRedemption> findByUserId(UUID userId);

  List<CouponRedemption> findByCouponId(UUID couponId);

  List<CouponRedemption> findByOrderId(UUID orderId);
}

