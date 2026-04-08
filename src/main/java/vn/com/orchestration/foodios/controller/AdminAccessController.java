package vn.com.orchestration.foodios.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.access.CreateAuthorityRequest;
import vn.com.orchestration.foodios.dto.access.CreateAuthorityResponse;
import vn.com.orchestration.foodios.dto.access.CreateRoleRequest;
import vn.com.orchestration.foodios.dto.access.CreateRoleResponse;
import vn.com.orchestration.foodios.dto.access.SetRoleAuthoritiesRequest;
import vn.com.orchestration.foodios.dto.access.SetRoleAuthoritiesResponse;
import vn.com.orchestration.foodios.dto.access.SetUserRoleRequest;
import vn.com.orchestration.foodios.dto.access.SetUserRoleResponse;
import vn.com.orchestration.foodios.service.access.AdminAccessService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.ADMIN_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.AUTHORITIES_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.ROLES_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.USERS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + ADMIN_PATH)
@RequiredArgsConstructor
public class AdminAccessController {

    private final AdminAccessService adminAccessService;

    @PostMapping(ROLES_PATH)
    public ResponseEntity<CreateRoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        CreateRoleResponse response = adminAccessService.createRole(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PostMapping(AUTHORITIES_PATH)
    public ResponseEntity<CreateAuthorityResponse> createAuthority(@Valid @RequestBody CreateAuthorityRequest request) {
        CreateAuthorityResponse response = adminAccessService.createAuthority(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PostMapping(USERS_PATH + ROLES_PATH)
    public ResponseEntity<SetUserRoleResponse> setUserRole(@Valid @RequestBody SetUserRoleRequest request) {
        SetUserRoleResponse response = adminAccessService.setUserRole(request);
        return HttpUtils.buildResponse(request, response);
    }

    @PostMapping(ROLES_PATH + AUTHORITIES_PATH)
    public ResponseEntity<SetRoleAuthoritiesResponse> setRoleAuthorities(
            @Valid @RequestBody SetRoleAuthoritiesRequest request) {
        SetRoleAuthoritiesResponse response = adminAccessService.setRoleAuthorities(request);
        return HttpUtils.buildResponse(request, response);
    }
}
