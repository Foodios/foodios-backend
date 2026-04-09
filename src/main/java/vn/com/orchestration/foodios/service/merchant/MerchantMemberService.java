package vn.com.orchestration.foodios.service.merchant;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.AddMerchantDriverRequest;
import vn.com.orchestration.foodios.dto.merchant.AddMerchantDriverResponse;
import vn.com.orchestration.foodios.dto.merchant.DeleteMerchantDriverResponse;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantDriversResponse;

import java.util.UUID;

public interface MerchantMemberService {
    AddMerchantDriverResponse addDriver(AddMerchantDriverRequest request);

    GetMerchantDriversResponse getDrivers(UUID merchantId, String query, BaseRequest request);

    DeleteMerchantDriverResponse deleteDriver(UUID merchantId, UUID userId, BaseRequest request);
}
