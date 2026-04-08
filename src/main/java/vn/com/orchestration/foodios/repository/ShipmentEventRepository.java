package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.shipping.ShipmentEvent;

import java.util.List;
import java.util.UUID;

public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, UUID> {
  List<ShipmentEvent> findByShipmentIdOrderByOccurredAtAsc(UUID shipmentId);
}

