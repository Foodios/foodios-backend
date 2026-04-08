package vn.com.orchestration.foodios.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.entity.cart.CartStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartPayload {
    private UUID id;
    private UUID userId;
    private UUID storeId;
    private CartStatus status;
    private Integer totalQuantity;
    private BigDecimal subtotal;
    private List<CartItemPayload> items;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
