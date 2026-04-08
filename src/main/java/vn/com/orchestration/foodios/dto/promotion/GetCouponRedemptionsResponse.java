package vn.com.orchestration.foodios.dto.promotion;

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
public class GetCouponRedemptionsResponse extends BaseResponse<GetCouponRedemptionsResponse.GetCouponRedemptionsResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetCouponRedemptionsResponseData {
        private List<CouponRedemptionPayload> redemptions;
    }
}
