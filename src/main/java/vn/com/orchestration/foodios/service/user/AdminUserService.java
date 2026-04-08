package vn.com.orchestration.foodios.service.user;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.user.GetUsersByRoleResponse;

import java.util.List;

public interface AdminUserService {
    GetUsersByRoleResponse getUsersByRoles(BaseRequest request, List<String> roleCodes, Integer pageNumber, Integer pageSize);
}
