package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.address.UserAddress;

public interface AddressRepository extends JpaRepository<UserAddress, UUID> {
  List<UserAddress> findByUserId(UUID userId);

  Optional<UserAddress> findByUserIdAndDefaultAddressTrue(UUID userId);
}
