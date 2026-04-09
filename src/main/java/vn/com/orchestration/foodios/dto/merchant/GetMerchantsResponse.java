package vn.com.orchestration.foodios.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetMerchantsResponse extends BaseResponse<GetMerchantsResponse.GetMerchantsResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetMerchantsResponseData {
        private List<MerchantPayload> items;
        private Integer pageNumber;
        private Integer pageSize;
        private Long totalItems;
        private Integer totalPages;
        private Boolean hasNext;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MerchantPayload {
        private String id;
        private String displayName;
        private String legalName;
        private String slug;
        private String logoUrl;
        private String description;
        private String cuisineCategory;
        private String contactEmail;
        private String supportHotline;
        private String status;
        private BigDecimal commissionRate;
        private String createdAt;
    }
}
