package vn.com.orchestration.foodios.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.merchant.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {}

