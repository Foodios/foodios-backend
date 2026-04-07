package vn.com.orchestration.foodios.service.loyalty.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.entity.loyalty.CustomerMembership;
import vn.com.orchestration.foodios.entity.loyalty.MembershipTier;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.repository.CustomerMembershipRepository;
import vn.com.orchestration.foodios.repository.MembershipTierRepository;
import vn.com.orchestration.foodios.service.loyalty.CustomerMembershipService;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerMembershipServiceImpl implements CustomerMembershipService {

    private static final String DEFAULT_TIER_CODE = "BRONZE";

    private final CustomerMembershipRepository customerMembershipRepository;
    private final MembershipTierRepository membershipTierRepository;

    public CustomerMembershipServiceImpl(
            CustomerMembershipRepository customerMembershipRepository,
            MembershipTierRepository membershipTierRepository
    ) {
        this.customerMembershipRepository = customerMembershipRepository;
        this.membershipTierRepository = membershipTierRepository;
    }

    @Override
    @Transactional
    public CustomerMembership createForNewCustomer(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User must be persisted before creating membership");
        }

        return customerMembershipRepository
                .findByUserId(user.getId())
                .orElseGet(() -> customerMembershipRepository.saveAndFlush(
                        createMembershipForNewUser(user)
                ));
    }

    private CustomerMembership createMembershipForNewUser(User user) {
        MembershipTier tier = membershipTierRepository
                .findByCode(DEFAULT_TIER_CODE)
                .orElseThrow(() -> new IllegalStateException("Membership tier BRONZE is not preloaded"));

        BigDecimal pointsToNextTier = calculatePointsToNextTier(tier, BigDecimal.ZERO);

        return CustomerMembership.builder()
                .user(user)
                .membershipTier(tier)
                .status("ACTIVE")
                .discountPercent(tier.getDiscountPercent())
                .pointMultiplier(tier.getPointMultiplier())
                .pointsToNextTier(pointsToNextTier)
                .currentAvailablePoints(BigDecimal.ZERO)
                .totalPoints(BigDecimal.ZERO)
                .build();
    }

    private BigDecimal calculatePointsToNextTier(MembershipTier currentTier, BigDecimal totalPoints) {
        List<MembershipTier> tiers = membershipTierRepository.findByEnabledTrueOrderByPriorityLevelAsc();
        if (tiers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        MembershipTier nextTier = null;
        for (MembershipTier tier : tiers) {
            if (tier == null) {
                continue;
            }
            if (tier.getPriorityLevel() > currentTier.getPriorityLevel()) {
                nextTier = tier;
                break;
            }
        }

        if (nextTier == null || nextTier.getMinPoints() == null) {
            return BigDecimal.ZERO;
        }
        if (totalPoints == null) {
            totalPoints = BigDecimal.ZERO;
        }
        BigDecimal diff = nextTier.getMinPoints().subtract(totalPoints);
        return diff.signum() < 0 ? BigDecimal.ZERO : diff;
    }
}
