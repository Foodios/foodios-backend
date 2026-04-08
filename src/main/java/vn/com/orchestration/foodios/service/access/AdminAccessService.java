package vn.com.orchestration.foodios.service.access;

import vn.com.orchestration.foodios.dto.access.CreateAuthorityRequest;
import vn.com.orchestration.foodios.dto.access.CreateAuthorityResponse;
import vn.com.orchestration.foodios.dto.access.CreateRoleRequest;
import vn.com.orchestration.foodios.dto.access.CreateRoleResponse;
import vn.com.orchestration.foodios.dto.access.SetRoleAuthoritiesRequest;
import vn.com.orchestration.foodios.dto.access.SetRoleAuthoritiesResponse;
import vn.com.orchestration.foodios.dto.access.SetUserRoleRequest;
import vn.com.orchestration.foodios.dto.access.SetUserRoleResponse;

public interface AdminAccessService {
    CreateRoleResponse createRole(CreateRoleRequest request);

    CreateAuthorityResponse createAuthority(CreateAuthorityRequest request);

    SetUserRoleResponse setUserRole(SetUserRoleRequest request);

    SetRoleAuthoritiesResponse setRoleAuthorities(SetRoleAuthoritiesRequest request);
}
