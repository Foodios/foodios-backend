package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.entity.merchant.MerchantStatus;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CreateMerchantResponse extends BaseResponse<CreateMerchantResponse.CreateMerchantResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateMerchantResponseData {
        private UUID merchantId;
        private UUID storeId;
        private String merchantName;
        private String merchantSlug;
        private String logoUrl;
        private String contactEmail;
        private String supportHotline;
        private String locationDistrict;
        private MerchantStatus merchantStatus;
        private StoreStatus storeStatus;
        private BigDecimal commissionRate;
    }
}
