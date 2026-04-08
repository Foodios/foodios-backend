package vn.com.orchestration.foodios.entity.promotion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.Store;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "coupons",
    indexes = {
      @Index(name = "idx_coupons_store_status_period", columnList = "store_id,status,starts_at,ends_at"),
      @Index(name = "idx_coupons_merchant_status_period", columnList = "merchant_id,status,starts_at,ends_at")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_coupons_merchant_code", columnNames = {"merchant_id", "code"}),
      @UniqueConstraint(name = "uk_coupons_store_code", columnNames = {"store_id", "code"})
    })
public class Coupon extends BaseEntity {

  @Column(name = "code", nullable = false, length = 40)
  private String code;

  @Column(name = "title", length = 160)
  private String title;

  @Column(name = "description", length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Builder.Default
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

  @Column(name = "is_stackable", nullable = false)
  @Builder.Default
  private boolean stackable = false;

  @Column(name = "is_auto_apply", nullable = false)
  @Builder.Default
  private boolean autoApply = false;

  @Column(name = "starts_at")
  private Instant startsAt;

  @Column(name = "ends_at")
  private Instant endsAt;

  @Column(name = "usage_limit")
  private Integer usageLimit;

  @Column(name = "per_user_limit")
  private Integer perUserLimit;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "status", nullable = false, length = 16)
  private CouponStatus status = CouponStatus.ACTIVE;
}
