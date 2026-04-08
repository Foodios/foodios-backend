package vn.com.orchestration.foodios.dto.review;

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
public class GetMerchantReviewsResponse extends BaseResponse<GetMerchantReviewsResponse.GetMerchantReviewsResponseData> {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GetMerchantReviewsResponseData {
        private ReviewSummaryPayload summary;
        private List<ReviewPayload> items;
        private Integer pageNumber;
        private Integer pageSize;
        private Long totalItems;
        private Integer totalPages;
        private Boolean hasNext;
    }
}
