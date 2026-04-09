package vn.com.orchestration.foodios.service.search.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.search.GlobalSearchResponse;
import vn.com.orchestration.foodios.repository.search.ProductSearchRepository;
import vn.com.orchestration.foodios.repository.search.StoreSearchRepository;
import vn.com.orchestration.foodios.service.search.GlobalSearchService;

import java.util.UUID;
import java.util.stream.Collectors;

import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final StoreSearchRepository storeSearchRepository;

    @Override
    public GlobalSearchResponse search(BaseRequest request, String query) {
        if (query == null || query.isBlank()) {
            return emptyResponse();
        }

        var stores = storeSearchRepository.findByNameContaining(query);
        var products = productSearchRepository.findByNameContaining(query);

        return GlobalSearchResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GlobalSearchResponse.GlobalSearchData.builder()
                        .stores(stores.stream().map(s -> GlobalSearchResponse.StoreSearchResult.builder()
                                .id(UUID.fromString(s.getId()))
                                .name(s.getName())
                                .description(s.getDescription())
                                .slug(s.getSlug())
                                .logoUrl(s.getLogoUrl())
                                .rating(s.getRating())
                                .totalReviews(s.getTotalReviews())
                                .build()).collect(Collectors.toList()))
                        .products(products.stream().map(p -> GlobalSearchResponse.ProductSearchResult.builder()
                                .id(UUID.fromString(p.getId()))
                                .name(p.getName())
                                .description(p.getDescription())
                                .slug(p.getSlug())
                                .price(p.getPrice())
                                .imageUrl(p.getImageUrl())
                                .storeId(UUID.fromString(p.getStoreId()))
                                .storeName(p.getStoreName())
                                .build()).collect(Collectors.toList()))
                        .build())
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
