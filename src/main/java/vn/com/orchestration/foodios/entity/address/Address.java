package vn.com.orchestration.foodios.entity.address;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import vn.com.orchestration.foodios.entity.common.AddressSnapshot;
import vn.com.orchestration.foodios.entity.common.BaseEntity;
import vn.com.orchestration.foodios.entity.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "label", length = 80)
  private String label;

  @Column(name = "is_default", nullable = false)
  private boolean defaultAddress;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "contactName", column = @Column(name = "contact_name", length = 120)),
    @AttributeOverride(
        name = "contactPhone",
        column = @Column(name = "contact_phone", length = 32)),
    @AttributeOverride(name = "line1", column = @Column(name = "line1", length = 255)),
    @AttributeOverride(name = "line2", column = @Column(name = "line2", length = 255)),
    @AttributeOverride(name = "ward", column = @Column(name = "ward", length = 120)),
    @AttributeOverride(name = "district", column = @Column(name = "district", length = 120)),
    @AttributeOverride(name = "city", column = @Column(name = "city", length = 120)),
    @AttributeOverride(name = "province", column = @Column(name = "province", length = 120)),
    @AttributeOverride(name = "postalCode", column = @Column(name = "postal_code", length = 20)),
    @AttributeOverride(name = "country", column = @Column(name = "country", length = 2)),
    @AttributeOverride(
        name = "latitude",
        column = @Column(name = "latitude", precision = 9, scale = 6)),
    @AttributeOverride(
        name = "longitude",
        column = @Column(name = "longitude", precision = 9, scale = 6))
  })
  private AddressSnapshot snapshot = new AddressSnapshot();
}
