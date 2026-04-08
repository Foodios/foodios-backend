package vn.com.orchestration.foodios.service.storefront;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.storefront.GetPublicMerchantsResponse;
import vn.com.orchestration.foodios.dto.storefront.GetMerchantStorefrontResponse;

public interface PublicMerchantStorefrontService {

    GetPublicMerchantsResponse getPublicMerchants(BaseRequest request);

    GetMerchantStorefrontResponse getMerchantStorefront(BaseRequest request, String slug);
}
