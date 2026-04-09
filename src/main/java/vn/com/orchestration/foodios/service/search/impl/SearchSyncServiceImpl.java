package vn.com.orchestration.foodios.service.search.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.document.ProductDoc;
import vn.com.orchestration.foodios.document.StoreDoc;
import vn.com.orchestration.foodios.entity.catalog.Product;
import vn.com.orchestration.foodios.entity.merchant.Store;
import vn.com.orchestration.foodios.repository.ProductRepository;
import vn.com.orchestration.foodios.repository.StoreRepository;
import vn.com.orchestration.foodios.repository.search.ProductSearchRepository;
import vn.com.orchestration.foodios.repository.search.StoreSearchRepository;
import vn.com.orchestration.foodios.service.search.SearchSyncService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchSyncServiceImpl implements SearchSyncService {

    private final ProductSearchRepository productSearchRepository;
    private final StoreSearchRepository storeSearchRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Override
    @Async
    @Transactional(readOnly = true)
    public void syncProduct(Product product) {
        try {
            ProductDoc doc = ProductDoc.builder()
                    .id(product.getId().toString())
                    .name(product.getName())
                    .description(product.getDescription())
                    .slug(product.getSlug())
                    .price(product.getPrice())
                    .imageUrl(product.getImageUrl())
                    .storeId(product.getStore().getId().toString())
                    .storeName(product.getStore().getName())
                    .categoryId(product.getCategory() != null ? product.getCategory().getId().toString() : null)
                    .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                    .status(product.getStatus().name())
                    .available(product.isAvailable())
                    .build();
            productSearchRepository.save(doc);
            log.info("Synced product to ES: {}", product.getName());
        } catch (Exception e) {
            log.error("Failed to sync product to ES: {}", product.getName(), e);
        }
    }

    @Override
    @Async
    public void deleteProduct(Product product) {
        productSearchRepository.deleteById(product.getId().toString());
    }

    @Override
    @Async
    @Transactional(readOnly = true)
    public void syncStore(Store store) {
        try {
            StoreDoc doc = StoreDoc.builder()
                    .id(store.getId().toString())
                    .name(store.getName())
                    .description(store.getMerchant().getDescription())
                    .slug(store.getSlug())
                    .logoUrl(store.getHeroImageUrl())
                    .status(store.getStatus().name())
                    .cuisineCategory(store.getMerchant().getCuisineCategory())
                    .address(store.getAddress() != null ? store.getAddress().getLine1() : null)
                    .build();
            storeSearchRepository.save(doc);
            log.info("Synced store to ES: {}", store.getName());
        } catch (Exception e) {
            log.error("Failed to sync store to ES: {}", store.getName(), e);
        }
    }

    @Override
    @Async
    public void deleteStore(Store store) {
        storeSearchRepository.deleteById(store.getId().toString());
    }

    @Override
    @Transactional(readOnly = true)
    public void syncAllProducts() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            syncProduct(product);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void syncAllStores() {
        List<Store> stores = storeRepository.findAll();
        for (Store store : stores) {
            syncStore(store);
        }
    }
}
