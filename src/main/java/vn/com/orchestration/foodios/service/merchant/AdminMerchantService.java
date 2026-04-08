package vn.com.orchestration.foodios.service.merchant;

import vn.com.orchestration.foodios.dto.merchant.CreateMerchantRequest;
import vn.com.orchestration.foodios.dto.merchant.CreateMerchantResponse;

public interface AdminMerchantService {
    CreateMerchantResponse createMerchant(CreateMerchantRequest request);
}
