package vn.com.orchestration.foodios.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class AddressSnapshot {

  @Column(length = 120)
  private String contactName;

  @Column(length = 32)
  private String contactPhone;

  @Column(length = 255)
  private String line1;

  @Column(length = 255)
  private String line2;

  @Column(length = 120)
  private String ward;

  @Column(length = 120)
  private String district;

  @Column(length = 120)
  private String city;

  @Column(length = 120)
  private String province;

  @Column(length = 20)
  private String postalCode;

  @Column(length = 2)
  private String country;

  @Column(precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitude;
}
