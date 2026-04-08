package vn.com.orchestration.foodios.dto.order;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.entity.order.OrderStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UpdateOrderStatusResponse extends BaseResponse<UpdateOrderStatusResponse.UpdateOrderStatusResponseData>{
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class UpdateOrderStatusResponseData {
        private OrderStatus status;
        private String creator;
        private OffsetDateTime processedAt;
    }
}
