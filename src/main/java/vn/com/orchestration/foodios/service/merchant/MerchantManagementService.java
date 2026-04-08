package vn.com.orchestration.foodios.service.merchant;

import vn.com.orchestration.foodios.dto.merchant.MerchantSignupRequest;
import vn.com.orchestration.foodios.dto.merchant.MerchantSignupResponse;

public interface MerchantManagementService {

    MerchantSignupResponse signup(MerchantSignupRequest merchantSignupRequest);
}
