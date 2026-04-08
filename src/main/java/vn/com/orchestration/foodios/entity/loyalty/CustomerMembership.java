package vn.com.orchestration.foodios.entity.loyalty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;
import vn.com.orchestration.foodios.entity.user.User;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "customer_memberships",
    uniqueConstraints = {@UniqueConstraint(name = "uk_customer_memberships_user", columnNames = {"user_id"})})
public class CustomerMembership extends BaseEntity {

  @ToString.Exclude
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "membership_tier_id", nullable = false)
  private MembershipTier membershipTier;

  @Column(name = "status", nullable = false, length = 16)
  @Builder.Default
  private String status = "ACTIVE";

  @Column(name = "joined_at", nullable = false)
  @Builder.Default
  private OffsetDateTime joinedAt = OffsetDateTime.now();

  @Column(name = "promoted_at", nullable = false)
  @Builder.Default
  private OffsetDateTime promotedAt = OffsetDateTime.now();

  @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal discountPercent = BigDecimal.ZERO;

  @Column(name = "points_to_next_tier", nullable = false, precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal pointsToNextTier = BigDecimal.ZERO;

  @Column(name = "point_multiplier", nullable = false, precision = 10, scale = 4)
  @Builder.Default
  private BigDecimal pointMultiplier = BigDecimal.ONE;

  @Column(name = "current_available_points", nullable = false, precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal currentAvailablePoints = BigDecimal.ZERO;

  @Column(name = "total_points", nullable = false, precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal totalPoints = BigDecimal.ZERO;

  @Column(name = "expires_at")
  private Instant expiresAt;
}
