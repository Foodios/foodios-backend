package vn.com.orchestration.foodios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.orchestration.foodios.entity.cart.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
  List<CartItem> findByCartId(UUID cartId);

  Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);
}

