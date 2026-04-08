package vn.com.orchestration.foodios.service.review;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.review.GetMerchantReviewResponse;
import vn.com.orchestration.foodios.dto.review.GetMerchantReviewsResponse;
import vn.com.orchestration.foodios.entity.review.ReviewSourceType;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;

import java.util.UUID;

public interface MerchantReviewService {

    GetMerchantReviewsResponse getReviews(
            BaseRequest request,
            UUID merchantId,
            UUID storeId,
            ReviewStatus status,
            ReviewSourceType sourceType,
            Integer pageNumber,
            Integer pageSize
    );

    GetMerchantReviewResponse getReviewDetail(BaseRequest request, UUID merchantId, UUID reviewId);
}
