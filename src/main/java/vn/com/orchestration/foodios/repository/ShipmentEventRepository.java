package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.shipping.ShipmentEvent;

public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, UUID> {
  List<ShipmentEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);
}

