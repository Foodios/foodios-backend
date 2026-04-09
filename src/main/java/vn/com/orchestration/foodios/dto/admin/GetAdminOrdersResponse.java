package vn.com.orchestration.foodios.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetAdminOrdersResponse extends BaseResponse<GetAdminOrdersResponse.GetAdminOrdersResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetAdminOrdersResponseData {
        private List<OrderPayload> items;
        private Long totalItems;
        private Long numberOfNewOrders;
        private Long numberOfPreparingOrder;
        private Long numberOfOnDelivery;
        private Long numberOfCancelled;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderPayload {
        private UUID id;
        private String code;
        private String customerName;
        private String merchantName;
        private String storeName;
        private BigDecimal total;
        private String status;
        private String serviceMethod;
        private String deliveryAddress;
        private OffsetDateTime createdAt;
    }
}
