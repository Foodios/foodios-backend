package vn.com.orchestration.foodios.entity.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "merchants",
    indexes = {
      @Index(name = "idx_merchants_status", columnList = "status")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_merchants_slug", columnNames = {"slug"})
    })
public class Merchant extends BaseEntity {

  @Column(name = "display_name", nullable = false, length = 160)
  private String displayName;

  @Column(name = "legal_name", length = 200)
  private String legalName;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "tax_code")
  private String taxCode;

  @Column(name = "business_registration_number")
  private String businessRegistrationNumber;

  @Column(name = "business_license_image_url")
  private String businessLicenseImageUrl;

  @Column(name = "food_safety_license_image_url")
  private String foodSafetyLicenseImageUrl;

  @Embedded
  private MerchantPayout merchantPayout;

  @Column(name = "slug", nullable = false, length = 180)
  private String slug;

  @Column(name = "logo_url", length = 500)
  private String logoUrl;

  @Column(name = "cuisine_category", length = 120)
  private String cuisineCategory;

  @Column(name = "contact_email", length = 254)
  private String contactEmail;

  @Column(name = "support_hotline", length = 32)
  private String supportHotline;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "status", nullable = false, length = 16)
  private MerchantStatus status = MerchantStatus.PENDING;
}
