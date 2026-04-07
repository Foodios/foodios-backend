package vn.com.orchestration.foodios.entity.promotion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import vn.com.orchestration.foodios.entity.common.BaseEntity;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.Store;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "coupons",
    uniqueConstraints = {@UniqueConstraint(name = "uk_coupons_code", columnNames = {"code"})})
public class Coupon extends BaseEntity {

  @Column(name = "code", nullable = false, length = 40)
  private String code;

  @Column(name = "title", length = 160)
  private String title;

  @Column(name = "description", length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope", nullable = false, length = 16)
  private CouponScope scope = CouponScope.STORE;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "merchant_id")
  private Merchant merchant;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id")
  private Store store;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type", nullable = false, length = 16)
  private DiscountType discountType;

  @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
  private BigDecimal discountValue;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency = "VND";

  @Column(name = "min_order_amount", precision = 19, scale = 2)
  private BigDecimal minOrderAmount;

  @Column(name = "max_discount_amount", precision = 19, scale = 2)
  private BigDecimal maxDiscountAmount;

  @Column(name = "starts_at")
  private Instant startsAt;

  @Column(name = "ends_at")
  private Instant endsAt;

  @Column(name = "usage_limit")
  private Integer usageLimit;

  @Column(name = "per_user_limit")
  private Integer perUserLimit;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private CouponStatus status = CouponStatus.ACTIVE;
}

