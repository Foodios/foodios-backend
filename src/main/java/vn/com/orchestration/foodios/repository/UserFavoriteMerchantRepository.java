package vn.com.orchestration.foodios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.user.UserFavoriteMerchant;

import java.util.Optional;
import java.util.UUID;

public interface UserFavoriteMerchantRepository extends JpaRepository<UserFavoriteMerchant, UUID> {
    Optional<UserFavoriteMerchant> findByUserIdAndMerchantId(UUID userId, UUID merchantId);
    Page<UserFavoriteMerchant> findByUserId(UUID userId, Pageable pageable);
    boolean existsByUserIdAndMerchantId(UUID userId, UUID merchantId);
    void deleteByUserIdAndMerchantId(UUID userId, UUID merchantId);
}
