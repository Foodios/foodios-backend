package vn.com.orchestration.foodios.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.order.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<FoodOrder, UUID> {
  Optional<FoodOrder> findByCode(String code);

  List<FoodOrder> findByCustomerId(UUID customerId);

  List<FoodOrder> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

  Page<FoodOrder> findByCustomerId(UUID customerId, Pageable pageable);

  Page<FoodOrder> findByCustomerIdAndStatus(UUID customerId, OrderStatus status, Pageable pageable);

  List<FoodOrder> findByStoreId(UUID storeId);

  Page<FoodOrder> findByStoreMerchantId(UUID merchantId, Pageable pageable);

  Page<FoodOrder> findByStoreMerchantIdAndStatus(UUID merchantId, OrderStatus status, Pageable pageable);

  Page<FoodOrder> findByStoreMerchantIdAndStatusIn(UUID merchantId, List<OrderStatus> statuses, Pageable pageable);

  Page<FoodOrder> findByStatus(OrderStatus status, Pageable pageable);

  Page<FoodOrder> findByStoreIdAndStatus(UUID storeId, OrderStatus status, Pageable pageable);
}
