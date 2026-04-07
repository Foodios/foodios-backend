package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.order.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
  List<OrderItem> findByOrderId(UUID orderId);
}

