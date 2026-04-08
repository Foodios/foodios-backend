package vn.com.orchestration.foodios.service.storefront.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.storefront.GetPublicMerchantsResponse;
import vn.com.orchestration.foodios.dto.storefront.PublicMerchantListItemPayload;
import vn.com.orchestration.foodios.dto.review.ReviewSummaryPayload;
import vn.com.orchestration.foodios.dto.store.StoreAddressPayload;
import vn.com.orchestration.foodios.dto.storefront.GetMerchantStorefrontResponse;
import vn.com.orchestration.foodios.dto.storefront.StorefrontMenuCategoryPayload;
import vn.com.orchestration.foodios.dto.storefront.StorefrontProductPayload;
import vn.com.orchestration.foodios.dto.storefront.StorefrontStoreLocationPayload;
import vn.com.orchestration.foodios.entity.catalog.Category;
import vn.com.orchestration.foodios.entity.catalog.CategoryStatus;
import vn.com.orchestration.foodios.entity.catalog.Product;
import vn.com.orchestration.foodios.entity.catalog.ProductStatus;
import vn.com.orchestration.foodios.entity.common.AddressSnapshot;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.merchant.MerchantStatus;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;
import vn.com.orchestration.foodios.entity.review.Review;
import vn.com.orchestration.foodios.entity.review.ReviewStatus;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.repository.CategoryRepository;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.ProductRepository;
import vn.com.orchestration.foodios.repository.ReviewRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.service.storefront.PublicMerchantStorefrontService;
import vn.com.orchestration.foodios.utils.ApiResultFactory;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PublicMerchantStorefrontServiceImpl implements PublicMerchantStorefrontService {

    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ApiResultFactory apiResultFactory;

    @Override
    @Transactional(readOnly = true)
    public GetPublicMerchantsResponse getPublicMerchants(BaseRequest request) {
        List<Merchant> merchants = merchantRepository.findByStatus(MerchantStatus.ACTIVE).stream()
                .sorted(Comparator.comparing(Merchant::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return GetPublicMerchantsResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetPublicMerchantsResponse.GetPublicMerchantsResponseData.builder()
                        .merchants(merchants.stream().map(this::toMerchantListItem).toList())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetMerchantStorefrontResponse getMerchantStorefront(BaseRequest request, String slug) {
        String normalizedSlug = trimToNull(slug);
        if (normalizedSlug == null) {
            throw businessException(request, "Merchant slug is required");
        }

        Merchant merchant = merchantRepository.findBySlug(normalizedSlug)
                .filter(item -> item.getStatus() == MerchantStatus.ACTIVE)
                .orElseThrow(() -> businessException(request, "Merchant not found"));
        UUID merchantId = merchant.getId();

        List<Store> stores = storeRepository.findByMerchantIdAndStatus(merchantId, StoreStatus.ACTIVE).stream()
                .sorted(Comparator.comparing(Store::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<Category> categories = categoryRepository.findByStoreMerchantIdAndStatusAndActiveTrue(merchantId, CategoryStatus.ACTIVE)
                .stream()
                .sorted(Comparator.comparing((Category item) -> item.getStore().getName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparingInt(Category::getSortOrder)
                        .thenComparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<UUID, List<Product>> productsByCategory = productRepository.findByStoreMerchantIdAndStatusAndAvailableTrue(merchantId, ProductStatus.ACTIVE)
                .stream()
                .sorted(Comparator.comparing((Product item) -> item.getStore().getName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparingInt(Product::getSortOrder)
                        .thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .filter(product -> product.getCategory() != null)
                .collect(Collectors.groupingBy(product -> product.getCategory().getId()));

        ReviewSummaryPayload overallReview = buildSummary(
                reviewRepository.findByStoreMerchantIdAndStatus(merchantId, ReviewStatus.PUBLISHED)
        );

        return GetMerchantStorefrontResponse.builder()
                .result(apiResultFactory.buildSuccess())
                .data(GetMerchantStorefrontResponse.GetMerchantStorefrontResponseData.builder()
                        .merchantId(merchant.getId())
                        .merchantName(merchant.getDisplayName())
                        .merchantSlug(merchant.getSlug())
                        .logoUrl(merchant.getLogoUrl())
                        .description(merchant.getDescription())
                        .overallReview(overallReview)
                        .storeLocations(stores.stream().map(this::toStoreLocationPayload).toList())
                        .menuByCategory(categories.stream()
                                .map(category -> toMenuCategoryPayload(
                                        category,
                                        productsByCategory.getOrDefault(category.getId(), List.of())
                                ))
                                .toList())
                        .build())
                .build();
    }

    private PublicMerchantListItemPayload toMerchantListItem(Merchant merchant) {
        List<Store> stores = storeRepository.findByMerchantIdAndStatus(merchant.getId(), StoreStatus.ACTIVE);
        return PublicMerchantListItemPayload.builder()
                .merchantId(merchant.getId())
                .merchantName(merchant.getDisplayName())
                .merchantSlug(merchant.getSlug())
                .logoUrl(merchant.getLogoUrl())
                .description(merchant.getDescription())
                .cuisineCategory(merchant.getCuisineCategory())
                .activeStoreCount(stores.size())
                .overallReview(buildSummary(
                        reviewRepository.findByStoreMerchantIdAndStatus(merchant.getId(), ReviewStatus.PUBLISHED)
                ))
                .build();
    }

    private ReviewSummaryPayload buildSummary(List<Review> reviews) {
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

    private StorefrontStoreLocationPayload toStoreLocationPayload(Store store) {
        return StorefrontStoreLocationPayload.builder()
                .id(store.getId())
                .name(store.getName())
                .phone(store.getPhone())
                .timeZone(store.getTimeZone())
                .heroImageUrl(store.getHeroImageUrl())
                .opensAt(store.getOpensAt())
                .closesAt(store.getClosesAt())
                .address(toAddressPayload(store.getAddress()))
                .build();
    }

    private StoreAddressPayload toAddressPayload(AddressSnapshot address) {
        if (address == null) {
            return null;
        }
        return StoreAddressPayload.builder()
                .contactName(address.getContactName())
                .contactPhone(address.getContactPhone())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .province(address.getProvince())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }

    private StorefrontMenuCategoryPayload toMenuCategoryPayload(Category category, List<Product> products) {
        return StorefrontMenuCategoryPayload.builder()
                .categoryId(category.getId())
                .storeId(category.getStore().getId())
                .storeName(category.getStore().getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .sortOrder(category.getSortOrder())
                .products(products.stream().map(this::toProductPayload).toList())
                .build();
    }

    private StorefrontProductPayload toProductPayload(Product product) {
        return StorefrontProductPayload.builder()
                .id(product.getId())
                .storeId(product.getStore().getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .currency(product.getCurrency())
                .imageUrl(product.getImageUrl())
                .featured(product.isFeatured())
                .available(product.isAvailable())
                .preparationTimeMinutes(product.getPreparationTimeMinutes())
                .build();
    }

    private BusinessException businessException(BaseRequest request, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(RECORD_NOT_FOUND, message)
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
