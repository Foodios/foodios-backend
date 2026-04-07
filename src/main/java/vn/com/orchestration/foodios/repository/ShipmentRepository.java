package vn.com.orchestration.foodios.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.shipping.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
  Optional<Shipment> findByOrderId(UUID orderId);
}

