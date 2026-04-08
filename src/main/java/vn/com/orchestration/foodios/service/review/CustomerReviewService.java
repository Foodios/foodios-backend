package vn.com.orchestration.foodios.service.review;

import vn.com.orchestration.foodios.dto.review.CreateOrderReviewRequest;
import vn.com.orchestration.foodios.dto.review.CreateOrderReviewResponse;
import vn.com.orchestration.foodios.dto.review.CreateStoreReviewRequest;
import vn.com.orchestration.foodios.dto.review.CreateStoreReviewResponse;

public interface CustomerReviewService {

    CreateStoreReviewResponse createStoreReview(CreateStoreReviewRequest request);

    CreateOrderReviewResponse createOrderReview(CreateOrderReviewRequest request);
}
