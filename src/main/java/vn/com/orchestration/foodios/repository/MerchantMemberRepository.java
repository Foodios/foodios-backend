package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.merchant.MerchantMember;

public interface MerchantMemberRepository extends JpaRepository<MerchantMember, UUID> {
  Optional<MerchantMember> findByMerchantIdAndUserId(UUID merchantId, UUID userId);

  List<MerchantMember> findByMerchantId(UUID merchantId);

  List<MerchantMember> findByUserId(UUID userId);

  boolean existsByMerchantIdAndUserId(UUID merchantId, UUID userId);
}

