package vn.com.orchestration.foodios.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlaceOrderRequest extends BaseRequest {

    @Valid
    @NotNull
    private PlaceOrderRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceOrderRequestData {
        @NotNull
        private UUID storeId;
        @NotBlank
        private String serviceMethod;
        @NotBlank
        private String paymentMethod;
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal totalAmount;
        @NotBlank
        @Size(max = 3)
        private String currency;
        @Valid
        private ShippingAddress shippingAddress;
        @Valid
        @NotEmpty
        private List<OrderItemInput> items;
        @Size(max = 500)
        private String notes;
        @Size(max = 40)
        private String promoCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingAddress {
        @NotBlank
        @Size(max = 255)
        private String fullAddress;
        @NotNull
        private BigDecimal latitude;
        @NotNull
        private BigDecimal longitude;
        @NotBlank
        @Size(max = 120)
        private String receiverName;
        @NotBlank
        @Size(max = 32)
        private String receiverPhone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemInput {
        @NotNull
        private UUID productId;
        @NotNull
        @Min(1)
        private Integer quantity;
        @NotNull
        @DecimalMin("0.00")
        private BigDecimal unitPrice;
    }
}
