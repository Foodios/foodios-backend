package vn.com.orchestration.foodios.entity.order;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import vn.com.orchestration.foodios.entity.common.AddressSnapshot;
import vn.com.orchestration.foodios.entity.common.BaseEntity;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.user.User;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "orders",
    uniqueConstraints = {@UniqueConstraint(name = "uk_orders_code", columnNames = {"code"})})
public class FoodOrder extends BaseEntity {

  @Column(name = "code", nullable = false, length = 32)
  private String code;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  private User customer;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 24)
  private OrderStatus status = OrderStatus.DRAFT;

  @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(name = "delivery_fee", nullable = false, precision = 19, scale = 2)
  private BigDecimal deliveryFee = BigDecimal.ZERO;

  @Column(name = "service_fee", nullable = false, precision = 19, scale = 2)
  private BigDecimal serviceFee = BigDecimal.ZERO;

  @Column(name = "total", nullable = false, precision = 19, scale = 2)
  private BigDecimal total = BigDecimal.ZERO;

  @Column(name = "notes", length = 500)
  private String notes;

  @Column(name = "placed_at")
  private Instant placedAt;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "contactName",
        column = @Column(name = "delivery_contact_name", length = 120)),
    @AttributeOverride(
        name = "contactPhone",
        column = @Column(name = "delivery_contact_phone", length = 32)),
    @AttributeOverride(name = "line1", column = @Column(name = "delivery_line1", length = 255)),
    @AttributeOverride(name = "line2", column = @Column(name = "delivery_line2", length = 255)),
    @AttributeOverride(name = "ward", column = @Column(name = "delivery_ward", length = 120)),
    @AttributeOverride(
        name = "district",
        column = @Column(name = "delivery_district", length = 120)),
    @AttributeOverride(name = "city", column = @Column(name = "delivery_city", length = 120)),
    @AttributeOverride(
        name = "province",
        column = @Column(name = "delivery_province", length = 120)),
    @AttributeOverride(
        name = "postalCode",
        column = @Column(name = "delivery_postal_code", length = 20)),
    @AttributeOverride(name = "country", column = @Column(name = "delivery_country", length = 2)),
    @AttributeOverride(
        name = "latitude",
        column = @Column(name = "delivery_latitude", precision = 9, scale = 6)),
    @AttributeOverride(
        name = "longitude",
        column = @Column(name = "delivery_longitude", precision = 9, scale = 6))
  })
  private AddressSnapshot deliveryAddress = new AddressSnapshot();
}

