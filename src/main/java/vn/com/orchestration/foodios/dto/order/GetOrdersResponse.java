package vn.com.orchestration.foodios.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetOrdersResponse extends BaseResponse<GetOrdersResponse.GetOrdersResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetOrdersResponseData {
        private List<OrderPayload> items;
        private Integer pageNumber;
        private Integer pageSize;
        private Long totalItems;
        private Integer totalPages;
        private Boolean hasNext;
    }
}
