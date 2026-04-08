package vn.com.orchestration.foodios.dto.storefront;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.orchestration.foodios.dto.review.ReviewSummaryPayload;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicMerchantListItemPayload {
    private UUID merchantId;
    private String merchantName;
    private String merchantSlug;
    private String logoUrl;
    private String description;
    private String cuisineCategory;
    private Integer activeStoreCount;
    private ReviewSummaryPayload overallReview;
}
