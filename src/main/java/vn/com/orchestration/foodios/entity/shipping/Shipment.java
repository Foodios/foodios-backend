package vn.com.orchestration.foodios.entity.shipping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "shipments",
    uniqueConstraints = {@UniqueConstraint(name = "uk_shipments_order", columnNames = {"order_id"})})
public class Shipment extends BaseEntity {

  @ToString.Exclude
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false, updatable = false)
  private FoodOrder order;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private ShipmentStatus status = ShipmentStatus.CREATED;

  @Column(name = "assigned_at")
  private Instant assignedAt;

  @Column(name = "picked_up_at")
  private Instant pickedUpAt;

  @Column(name = "delivered_at")
  private Instant deliveredAt;

  @Column(name = "failure_reason", length = 255)
  private String failureReason;
}

