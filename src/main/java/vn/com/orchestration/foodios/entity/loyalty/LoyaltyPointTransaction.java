package vn.com.orchestration.foodios.entity.loyalty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import vn.com.orchestration.foodios.entity.common.BaseEntity;
import vn.com.orchestration.foodios.entity.order.FoodOrder;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "loyalty_point_transactions")
public class LoyaltyPointTransaction extends BaseEntity {

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private LoyaltyAccount account;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private FoodOrder order;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 16)
  private LoyaltyPointTransactionType type;

  @Column(name = "points", nullable = false)
  private long points;

  @Column(name = "note", length = 255)
  private String note;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt = Instant.now();
}

