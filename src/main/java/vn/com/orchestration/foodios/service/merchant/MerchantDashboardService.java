package vn.com.orchestration.foodios.service.merchant;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.MerchantDashboardResponse;

import java.util.UUID;

public interface MerchantDashboardService {
    MerchantDashboardResponse getDashboardData(UUID merchantId, BaseRequest request);
}
