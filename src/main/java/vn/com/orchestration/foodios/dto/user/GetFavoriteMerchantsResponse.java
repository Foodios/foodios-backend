package vn.com.orchestration.foodios.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantsResponse;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetFavoriteMerchantsResponse extends BaseResponse<GetFavoriteMerchantsResponse.GetFavoriteMerchantsResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetFavoriteMerchantsResponseData {
        private List<GetMerchantsResponse.MerchantPayload> items;
        private Integer pageNumber;
        private Integer pageSize;
        private Long totalItems;
        private Integer totalPages;
        private Boolean hasNext;
    }
}
