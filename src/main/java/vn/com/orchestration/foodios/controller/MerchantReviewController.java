package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.review.GetMerchantReviewResponse;
import vn.com.orchestration.foodios.dto.review.GetMerchantReviewsResponse;
import vn.com.orchestration.foodios.entity.review.ReviewSourceType;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.review.MerchantReviewService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.MERCHANT_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.REVIEWS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + MERCHANT_PATH)
@RequiredArgsConstructor
public class MerchantReviewController {

    private final MerchantReviewService merchantReviewService;

    @GetMapping(REVIEWS_PATH)
    public ResponseEntity<GetMerchantReviewsResponse> getReviews(
            @RequestParam UUID merchantId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) ReviewSourceType sourceType,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMerchantReviewsResponse response = merchantReviewService.getReviews(
                baseRequest,
                merchantId,
                storeId,
                status,
                sourceType,
                pageNumber,
                pageSize
        );
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping(REVIEWS_PATH + "/{reviewId}")
    public ResponseEntity<GetMerchantReviewResponse> getReviewDetail(
            @PathVariable UUID reviewId,
            @RequestParam UUID merchantId,
            HttpServletRequest request
    ) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetMerchantReviewResponse response = merchantReviewService.getReviewDetail(baseRequest, merchantId, reviewId);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
