package vn.com.orchestration.foodios.service.review.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.review.CreateOrderReviewRequest;
import vn.com.orchestration.foodios.dto.review.CreateOrderReviewResponse;
import vn.com.orchestration.foodios.dto.review.CreateStoreReviewRequest;
import vn.com.orchestration.foodios.dto.review.CreateStoreReviewResponse;
import vn.com.orchestration.foodios.dto.review.ReviewPayload;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.order.FoodOrder;
import vn.com.orchestration.foodios.entity.order.OrderStatus;
import vn.com.orchestration.foodios.entity.review.Review;
import vn.com.orchestration.foodios.entity.review.ReviewSourceType;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.OrderRepository;
import vn.com.orchestration.foodios.repository.ReviewRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.review.CustomerReviewService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.STORE_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class CustomerReviewServiceImpl implements CustomerReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IdentityUserContextProvider identityUserContextProvider;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional
    public CreateStoreReviewResponse createStoreReview(CreateStoreReviewRequest request) {
        CreateStoreReviewRequest.CreateStoreReviewRequestData data = requireData(request, request.getData());
        User customer = resolveCurrentUser(request);
        Store store = storeRepository.findById(data.getStoreId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, STORE_NOT_FOUND_MESSAGE));

        Review review = Review.builder()
                .store(store)
                .customer(customer)
                .order(null)
                .sourceType(ReviewSourceType.STORE)
                .rating(data.getRating())
                .title(trimToNull(data.getTitle()))
                .comment(trimToNull(data.getComment()))
                .status(ReviewStatus.PUBLISHED)
                .build();

        Review savedReview = reviewRepository.saveAndFlush(review);
        return CreateStoreReviewResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedReview))
                .build();
    }

    @Override
    @Transactional
    public CreateOrderReviewResponse createOrderReview(CreateOrderReviewRequest request) {
        CreateOrderReviewRequest.CreateOrderReviewRequestData data = requireData(request, request.getData());
        User customer = resolveCurrentUser(request);
        FoodOrder order = orderRepository.findById(data.getOrderId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Order not found"));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw businessException(request, INVALID_INPUT_ERROR, "You can only review your own order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw businessException(request, INVALID_INPUT_ERROR, "Order must be delivered before review");
        }
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw businessException(request, INVALID_INPUT_ERROR, "Order already has a review");
        }

        Review review = Review.builder()
                .store(order.getStore())
                .customer(customer)
                .order(order)
                .sourceType(ReviewSourceType.ORDER)
                .rating(data.getRating())
                .title(trimToNull(data.getTitle()))
                .comment(trimToNull(data.getComment()))
                .status(ReviewStatus.PUBLISHED)
                .build();

        Review savedReview = reviewRepository.saveAndFlush(review);
        return CreateOrderReviewResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(toPayload(savedReview))
                .build();
    }

    private <T> T requireData(BaseRequest request, T data) {
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }
        return data;
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

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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
