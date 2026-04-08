package vn.com.orchestration.foodios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.cart.CartItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
  List<CartItem> findByCartId(UUID cartId);

  Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);
}

