package vn.com.orchestration.foodios.service.search.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.search.GlobalSearchResponse;
import vn.com.orchestration.foodios.entity.catalog.Product;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.entity.review.Review;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;
import vn.com.orchestration.foodios.repository.ProductRepository;
import vn.com.orchestration.foodios.repository.ReviewRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.service.search.GlobalSearchService;

import java.util.List;

import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public GlobalSearchResponse search(BaseRequest request, String query) {
        if (query == null || query.isBlank()) {
            return emptyResponse();
        }

        List<Store> stores =
                storeRepository.findTop10ByStatusAndNameContainingIgnoreCaseOrderByNameAsc(
                        StoreStatus.ACTIVE, query);
        List<Product> products =
                productRepository.findTop10ByStatusAndAvailableTrueAndNameContainingIgnoreCaseOrderByNameAsc(
                        ProductStatus.ACTIVE, query);

        return GlobalSearchResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GlobalSearchResponse.GlobalSearchData.builder()
                        .stores(stores.stream().map(this::toStoreResult).toList())
                        .products(products.stream().map(this::toProductResult).toList())
                        .build())
                .build();
    }

    private GlobalSearchResponse.StoreSearchResult toStoreResult(Store store) {
        List<Review> reviews = reviewRepository.findByStoreIdAndStatus(store.getId(), ReviewStatus.PUBLISHED);
        double rating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        return GlobalSearchResponse.StoreSearchResult.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getMerchant() != null ? store.getMerchant().getDescription() : null)
                .slug(store.getSlug())
                .logoUrl(store.getHeroImageUrl())
                .rating(rating)
                .totalReviews(reviews.size())
                .build();
    }

    private GlobalSearchResponse.ProductSearchResult toProductResult(Product product) {
        return GlobalSearchResponse.ProductSearchResult.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .slug(product.getSlug())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .storeId(product.getStore().getId())
                .storeName(product.getStore().getName())
                .build();
    }

    private GlobalSearchResponse emptyResponse() {
        return GlobalSearchResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GlobalSearchResponse.GlobalSearchData.builder()
                        .stores(java.util.List.of())
                        .products(java.util.List.of())
                        .build())
                .build();
    }
}
