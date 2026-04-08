package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class DeleteMerchantResponse extends BaseResponse<DeleteMerchantResponse.DeleteMerchantResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeleteMerchantResponseData {
        private String id;
        private String status;
    }
}
