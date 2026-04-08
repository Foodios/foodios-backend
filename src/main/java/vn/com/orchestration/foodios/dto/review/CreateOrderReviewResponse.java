package vn.com.orchestration.foodios.dto.review;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CreateOrderReviewResponse extends BaseResponse<ReviewPayload> {
}
