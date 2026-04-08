package vn.com.orchestration.foodios.service.merchant;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.store.CreateStoreRequest;
import vn.com.orchestration.foodios.dto.store.CreateStoreResponse;
import vn.com.orchestration.foodios.dto.store.DeleteStoreResponse;
import vn.com.orchestration.foodios.dto.store.GetStoreResponse;
import vn.com.orchestration.foodios.dto.store.GetStoresResponse;
import vn.com.orchestration.foodios.dto.store.UpdateStoreRequest;
import vn.com.orchestration.foodios.dto.store.UpdateStoreResponse;
import vn.com.orchestration.foodios.entity.merchant.StoreStatus;

import java.util.UUID;

public interface MerchantStoreService {

    CreateStoreResponse createStore(CreateStoreRequest request);

    UpdateStoreResponse updateStore(UUID storeId, UpdateStoreRequest request);

    DeleteStoreResponse deleteStore(UUID storeId, BaseRequest request, UUID merchantId);

    GetStoreResponse getStore(UUID storeId, BaseRequest request, UUID merchantId);

    GetStoresResponse getStores(BaseRequest request, UUID merchantId, StoreStatus status);
}
