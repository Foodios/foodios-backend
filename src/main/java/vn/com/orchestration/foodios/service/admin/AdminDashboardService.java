package vn.com.orchestration.foodios.service.admin;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.admin.GlobalDashboardResponse;

public interface AdminDashboardService {
    GlobalDashboardResponse getGlobalDashboardData(BaseRequest request);
}
