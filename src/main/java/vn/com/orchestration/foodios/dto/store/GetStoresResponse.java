package vn.com.orchestration.foodios.dto.store;

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
public class GetStoresResponse extends BaseResponse<GetStoresResponse.GetStoresResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetStoresResponseData {
        private List<StorePayload> stores;
    }
}
