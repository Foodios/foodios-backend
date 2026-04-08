package vn.com.orchestration.foodios.dto.store;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetStoreResponse extends BaseResponse<StorePayload> {
}
