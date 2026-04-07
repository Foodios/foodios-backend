package vn.com.orchestration.foodios.entity.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.entity.common.BaseEntity;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "merchants")
public class Merchant extends BaseEntity {

  @Column(name = "display_name", nullable = false, length = 160)
  private String displayName;

  @Column(name = "legal_name", length = 200)
  private String legalName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private MerchantStatus status = MerchantStatus.PENDING;
}

