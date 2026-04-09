package vn.com.orchestration.foodios.service.search;

import vn.com.orchestration.foodios.entity.catalog.Product;
import vn.com.orchestration.foodios.entity.merchant.Store;

public interface SearchSyncService {
    void syncProduct(Product product);
    void deleteProduct(Product product);
    void syncStore(Store store);
    void deleteStore(Store store);
    
    // Batch sync
    void syncAllProducts();
    void syncAllStores();
}
