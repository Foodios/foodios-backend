package vn.com.orchestration.foodios.entity.merchant;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import vn.com.orchestration.foodios.entity.common.AddressSnapshot;
import vn.com.orchestration.foodios.entity.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "stores",
    uniqueConstraints = {@UniqueConstraint(name = "uk_stores_slug", columnNames = {"slug"})})
public class Store extends BaseEntity {

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "merchant_id", nullable = false)
  private Merchant merchant;

  @Column(name = "name", nullable = false, length = 160)
  private String name;

  @Column(name = "slug", nullable = false, length = 180)
  private String slug;

  @Column(name = "phone", length = 32)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private StoreStatus status = StoreStatus.DRAFT;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "contactName",
        column = @Column(name = "address_contact_name", length = 120)),
    @AttributeOverride(
        name = "contactPhone",
        column = @Column(name = "address_contact_phone", length = 32)),
    @AttributeOverride(name = "line1", column = @Column(name = "address_line1", length = 255)),
    @AttributeOverride(name = "line2", column = @Column(name = "address_line2", length = 255)),
    @AttributeOverride(name = "ward", column = @Column(name = "address_ward", length = 120)),
    @AttributeOverride(
        name = "district",
        column = @Column(name = "address_district", length = 120)),
    @AttributeOverride(name = "city", column = @Column(name = "address_city", length = 120)),
    @AttributeOverride(
        name = "province",
        column = @Column(name = "address_province", length = 120)),
    @AttributeOverride(
        name = "postalCode",
        column = @Column(name = "address_postal_code", length = 20)),
    @AttributeOverride(name = "country", column = @Column(name = "address_country", length = 2)),
    @AttributeOverride(
        name = "latitude",
        column = @Column(name = "address_latitude", precision = 9, scale = 6)),
    @AttributeOverride(
        name = "longitude",
        column = @Column(name = "address_longitude", precision = 9, scale = 6))
  })
  private AddressSnapshot address = new AddressSnapshot();
}

