package vn.com.orchestration.foodios.dto.product;

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
public class GetProductsResponse extends BaseResponse<GetProductsResponse.GetProductsResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetProductsResponseData {
        private List<ProductPayload> products;
    }
}
