package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.loyalty.MembershipTier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipTierRepository extends JpaRepository<MembershipTier, UUID> {
    Optional<MembershipTier> findByCode(String code);

    List<MembershipTier> findByEnabledTrueOrderByPriorityLevelAsc();
}

