package vn.com.orchestration.foodios.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "payments",
    uniqueConstraints = {@UniqueConstraint(name = "uk_payments_order", columnNames = {"order_id"})})
public class Payment extends BaseEntity {

  @ToString.Exclude
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private FoodOrder order;

  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false, length = 24)
  private PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "status", nullable = false, length = 16)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(name = "provider", length = 64)
  private String provider;

  @Column(name = "provider_txn_id", length = 128)
  private String providerTxnId;

  @Column(name = "paid_at")
  private Instant paidAt;
}
