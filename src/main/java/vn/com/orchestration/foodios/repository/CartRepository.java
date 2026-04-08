package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.cart.Cart;
import vn.com.orchestration.foodios.entity.cart.CartStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
  Optional<Cart> findByUserIdAndStoreId(UUID userId, UUID storeId);

  List<Cart> findByUserIdAndStatus(UUID userId, CartStatus status);
}

