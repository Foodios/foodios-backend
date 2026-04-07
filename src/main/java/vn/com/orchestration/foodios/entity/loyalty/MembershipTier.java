package vn.com.orchestration.foodios.entity.loyalty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.entity.common.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(
    name = "membership_tier",
    uniqueConstraints = {@UniqueConstraint(name = "uk_membership_tier_code", columnNames = {"code"})})
public class MembershipTier extends BaseEntity {

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "badge", length = 80)
    private String badge;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "min_points", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal minPoints = BigDecimal.ZERO;

    @Column(name = "point_multiplier", nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal pointMultiplier = BigDecimal.ONE;

    @Column(name = "priority_level", nullable = false)
    private int priorityLevel;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}

