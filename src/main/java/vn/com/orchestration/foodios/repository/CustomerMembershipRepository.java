package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.loyalty.CustomerMembership;

import java.util.Optional;
import java.util.UUID;

public interface CustomerMembershipRepository extends JpaRepository<CustomerMembership, UUID> {
  Optional<CustomerMembership> findByUserId(UUID userId);
}

