package vn.com.orchestration.foodios.service.order.impl;

import lombok.experimental.UtilityClass;
import vn.com.orchestration.foodios.dto.order.OrderItemPayload;
import vn.com.orchestration.foodios.dto.order.OrderPayload;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.order.OrderItem;
import vn.com.orchestration.foodios.entity.order.Payment;

import java.util.List;

@UtilityClass
public class CustomerOrderMapper {

    public OrderPayload toPayload(FoodOrder order, List<OrderItem> items, Payment payment) {
        return OrderPayload.builder()
                .id(order.getId())
                .code(order.getCode())
                .storeId(order.getStore().getId())
                .storeName(order.getStore().getName())
                .storeLogo(order.getStore().getHeroImageUrl())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getFullName())
                .customerPhone(order.getCustomer().getPhone())
                .customerUrl(order.getCustomer().getAvatarUrl())
                .status(order.getStatus())
                .serviceMethod(order.getServiceMethod())
                .paymentMethod(payment != null ? payment.getMethod() : null)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .serviceFee(order.getServiceFee())
                .discountAmount(order.getDiscountAmount())
                .total(order.getTotal())
                .currency(order.getCurrency())
                .appliedCouponCode(order.getAppliedCouponCode())
                .notes(order.getNotes())
                .placedAt(order.getPlacedAt())
                .items(items.stream().map(CustomerOrderMapper::toItemPayload).toList())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemPayload toItemPayload(OrderItem item) {
        return OrderItemPayload.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
