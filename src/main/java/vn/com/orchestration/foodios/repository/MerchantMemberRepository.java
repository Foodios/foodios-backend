package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.merchant.MerchantMember;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MerchantMemberRepository extends JpaRepository<MerchantMember, UUID> {
  Optional<MerchantMember> findByMerchantIdAndUserId(UUID merchantId, UUID userId);

  List<MerchantMember> findByMerchantId(UUID merchantId);

  List<MerchantMember> findByUserId(UUID userId);

  boolean existsByMerchantIdAndUserId(UUID merchantId, UUID userId);

  boolean existsByMerchantIdAndUserIdAndStatus(
      UUID merchantId, UUID userId, MerchantMemberStatus status);

  boolean existsByMerchantIdAndUserIdAndStatusAndRoleIn(
      UUID merchantId,
      UUID userId,
      MerchantMemberStatus status,
      Collection<MerchantMemberRole> roles);
}
