package vn.com.orchestration.foodios.dto.promotion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UpdateCouponResponse extends BaseResponse<CouponPayload> {
}
