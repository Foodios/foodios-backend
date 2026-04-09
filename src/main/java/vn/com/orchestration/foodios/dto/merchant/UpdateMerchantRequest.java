package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseRequest;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateMerchantRequest extends BaseRequest {
    private UpdateMerchantRequestData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateMerchantRequestData {
        private String id;
        private String displayName;
        private String legalName;
        private String description;
        private String slug;
        private String logoUrl;
        private String cuisineCategory;
        private String contactEmail;
        private String supportHotline;
        private String status;
        private BigDecimal commissionRate;
    }
}
