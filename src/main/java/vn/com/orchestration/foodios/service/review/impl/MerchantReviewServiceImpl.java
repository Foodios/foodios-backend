package vn.com.orchestration.foodios.service.review.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.review.GetMerchantReviewResponse;
import vn.com.orchestration.foodios.dto.review.GetMerchantReviewsResponse;
import vn.com.orchestration.foodios.dto.review.ReviewPayload;
import vn.com.orchestration.foodios.dto.review.ReviewSummaryPayload;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberRole;
import vn.com.orchestration.foodios.entity.merchant.MerchantMemberStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.review.Review;
import vn.com.orchestration.foodios.entity.review.ReviewSourceType;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.MerchantMemberRepository;
import vn.com.orchestration.foodios.repository.ReviewRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.review.MerchantReviewService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.util.List;
import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_NUMBER;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_PAGE_SIZE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.MERCHANT_ACCESS_DENIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class MerchantReviewServiceImpl implements MerchantReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final MerchantMemberRepository merchantMemberRepository;
    private final UserRepository userRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional(readOnly = true)
    public GetMerchantReviewsResponse getReviews(
            BaseRequest request,
            UUID merchantId,
            UUID storeId,
            ReviewStatus status,
            ReviewSourceType sourceType,
            Integer pageNumber,
            Integer pageSize
    ) {
        validatePagination(request, pageNumber, pageSize);
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());
        if (storeId != null) {
            validateStoreScope(request, merchantId, storeId);
        }

        PageRequest pageable = PageRequest.of(
                pageNumber - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "reviewedAt", "createdAt")
        );
        Page<Review> reviews = loadReviews(merchantId, storeId, status, sourceType, pageable);
        ReviewSummaryPayload summary = buildSummary(merchantId, storeId);

        return GetMerchantReviewsResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetMerchantReviewsResponse.GetMerchantReviewsResponseData.builder()
                        .summary(summary)
                        .items(reviews.getContent().stream().map(this::toPayload).toList())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalItems(reviews.getTotalElements())
                        .totalPages(reviews.getTotalPages())
                        .hasNext(reviews.hasNext())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetMerchantReviewResponse getReviewDetail(BaseRequest request, UUID merchantId, UUID reviewId) {
        User currentUser = resolveCurrentUser(request);
        authorizeMerchantAccess(request, merchantId, currentUser.getId());

        Review review = reviewRepository.findById(reviewId)
                .filter(item -> item.getStore().getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Review not found"));

        return GetMerchantReviewResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(review))
                .build();
    }

    private Page<Review> loadReviews(
            UUID merchantId,
            UUID storeId,
            ReviewStatus status,
            ReviewSourceType sourceType,
            PageRequest pageable
    ) {
        if (storeId != null && status != null && sourceType != null) {
            return reviewRepository.findByStoreMerchantIdAndStoreIdAndStatusAndSourceType(
                    merchantId, storeId, status, sourceType, pageable);
        }
        if (storeId != null && status != null) {
            return reviewRepository.findByStoreMerchantIdAndStoreIdAndStatus(merchantId, storeId, status, pageable);
        }
        if (storeId != null && sourceType != null) {
            return reviewRepository.findByStoreMerchantIdAndStoreIdAndSourceType(merchantId, storeId, sourceType, pageable);
        }
        if (status != null && sourceType != null) {
            return reviewRepository.findByStoreMerchantIdAndStatusAndSourceType(merchantId, status, sourceType, pageable);
        }
        if (storeId != null) {
            return reviewRepository.findByStoreMerchantIdAndStoreId(merchantId, storeId, pageable);
        }
        if (status != null) {
            return reviewRepository.findByStoreMerchantIdAndStatus(merchantId, status, pageable);
        }
        if (sourceType != null) {
            return reviewRepository.findByStoreMerchantIdAndSourceType(merchantId, sourceType, pageable);
        }
        return reviewRepository.findByStoreMerchantId(merchantId, pageable);
    }

    private ReviewSummaryPayload buildSummary(UUID merchantId, UUID storeId) {
        List<Review> reviews = storeId == null
                ? reviewRepository.findByStoreMerchantIdAndStatus(merchantId, ReviewStatus.PUBLISHED)
                : reviewRepository.findByStoreMerchantIdAndStoreIdAndStatus(merchantId, storeId, ReviewStatus.PUBLISHED);

        long totalReviews = reviews.size();
        double averageRating = totalReviews == 0
                ? 0D
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0D);

        return ReviewSummaryPayload.builder()
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .totalReviews(totalReviews)
                .fiveStarCount(countByRating(reviews, 5))
                .fourStarCount(countByRating(reviews, 4))
                .threeStarCount(countByRating(reviews, 3))
                .twoStarCount(countByRating(reviews, 2))
                .oneStarCount(countByRating(reviews, 1))
                .build();
    }

    private long countByRating(List<Review> reviews, int rating) {
        return reviews.stream().filter(review -> review.getRating() != null && review.getRating() == rating).count();
    }

    private void validatePagination(BaseRequest request, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 1) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_NUMBER);
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_SIZE);
        }
    }

    private void validateStoreScope(BaseRequest request, UUID merchantId, UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));
        if (!store.getMerchant().getId().equals(merchantId)) {
            throw businessException(request, INVALID_INPUT_ERROR, "Store does not belong to merchant");
        }
    }

    private User resolveCurrentUser(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isBlank()) {
            return userRepository.findByEmail(currentUser.email())
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        }
        try {
            UUID userId = UUID.fromString(currentUser.subject());
            return userRepository.findById(userId)
                    .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));
        } catch (IllegalArgumentException exception) {
            throw businessException(request, INVALID_INPUT_ERROR, "Invalid current user");
        }
    }

    private void authorizeMerchantAccess(BaseRequest request, UUID merchantId, UUID userId) {
        boolean hasAccess = merchantMemberRepository.existsByMerchantIdAndUserIdAndStatusAndRoleIn(
                merchantId,
                userId,
                MerchantMemberStatus.ACTIVE,
                List.of(MerchantMemberRole.OWNER, MerchantMemberRole.MANAGER)
        );
        if (!hasAccess) {
            throw businessException(request, INVALID_INPUT_ERROR, MERCHANT_ACCESS_DENIED_MESSAGE);
        }
    }

    private ReviewPayload toPayload(Review review) {
        return ReviewPayload.builder()
                .id(review.getId())
                .merchantId(review.getStore().getMerchant().getId())
                .storeId(review.getStore().getId())
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getFullName())
                .customerAvatarUrl(review.getCustomer().getAvatarUrl())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .sourceType(review.getSourceType())
                .status(review.getStatus())
                .reviewedAt(review.getReviewedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }
}
