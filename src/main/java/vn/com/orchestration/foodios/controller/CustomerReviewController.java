package vn.com.orchestration.foodios.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.review.CreateOrderReviewRequest;
import vn.com.orchestration.foodios.dto.review.CreateOrderReviewResponse;
import vn.com.orchestration.foodios.dto.review.CreateStoreReviewRequest;
import vn.com.orchestration.foodios.dto.review.CreateStoreReviewResponse;
import vn.com.orchestration.foodios.service.review.CustomerReviewService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.REVIEWS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + REVIEWS_PATH)
@RequiredArgsConstructor
public class CustomerReviewController {

    private final CustomerReviewService customerReviewService;

    @PostMapping("/store")
    public ResponseEntity<CreateStoreReviewResponse> createStoreReview(
            @Valid @RequestBody CreateStoreReviewRequest request
    ) {
        CreateStoreReviewResponse response = customerReviewService.createStoreReview(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PostMapping("/order")
    public ResponseEntity<CreateOrderReviewResponse> createOrderReview(
            @Valid @RequestBody CreateOrderReviewRequest request
    ) {
        CreateOrderReviewResponse response = customerReviewService.createOrderReview(request);
        return HttpUtils.buildResponse(request, response);
    }
}
