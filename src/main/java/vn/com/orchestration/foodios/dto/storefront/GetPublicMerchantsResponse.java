package vn.com.orchestration.foodios.dto.storefront;

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
public class GetPublicMerchantsResponse extends BaseResponse<GetPublicMerchantsResponse.GetPublicMerchantsResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetPublicMerchantsResponseData {
        private List<PublicMerchantListItemPayload> merchants;
    }
}
