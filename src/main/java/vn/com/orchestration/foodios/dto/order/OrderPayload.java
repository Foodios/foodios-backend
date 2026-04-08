package vn.com.orchestration.foodios.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.entity.order.PaymentMethod;
import vn.com.orchestration.foodios.entity.order.PaymentStatus;
import vn.com.orchestration.foodios.entity.order.ServiceMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPayload {
    private UUID id;
    private String code;
    private UUID storeId;
    private String storeName;
    private String storeLogo;
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private String customerUrl;
    private OrderStatus status;
    private ServiceMethod serviceMethod;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String currency;
    private String appliedCouponCode;
    private String notes;
    private Instant placedAt;
    private List<OrderItemPayload> items;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
