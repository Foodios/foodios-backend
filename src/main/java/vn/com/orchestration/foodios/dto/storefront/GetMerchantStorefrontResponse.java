package vn.com.orchestration.foodios.dto.storefront;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.com.orchestration.foodios.dto.common.BaseResponse;
import vn.com.orchestration.foodios.dto.review.ReviewSummaryPayload;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class GetMerchantStorefrontResponse extends BaseResponse<GetMerchantStorefrontResponse.GetMerchantStorefrontResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetMerchantStorefrontResponseData {
        private UUID merchantId;
        private String merchantName;
        private String merchantSlug;
        private String logoUrl;
        private String description;
        private ReviewSummaryPayload overallReview;
        private List<StorefrontStoreLocationPayload> storeLocations;
        private List<StorefrontMenuCategoryPayload> menuByCategory;
    }
}
