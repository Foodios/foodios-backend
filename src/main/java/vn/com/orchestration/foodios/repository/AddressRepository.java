package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.address.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {
  List<Address> findByUserId(UUID userId);

  Optional<Address> findByUserIdAndDefaultAddressTrue(UUID userId);
}
