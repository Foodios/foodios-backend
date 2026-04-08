package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.user.GetUsersByRoleResponse;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.user.AdminUserService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import java.util.List;

import static vn.com.orchestration.foodios.constant.ApiConstant.ADMIN_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.USERS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + ADMIN_PATH + USERS_PATH)
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/fetch-customers")
    public ResponseEntity<GetUsersByRoleResponse> fetchCustomers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetUsersByRoleResponse response = adminUserService.getUsersByRoles(
                baseRequest, List.of("CUSTOMER"), pageNumber, pageSize);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping("/fetch-merchants")
    public ResponseEntity<GetUsersByRoleResponse> fetchMerchants(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        List<String> merchantRoles = List.of(
                "MERCHANT_OWNER", "MERCHANT_MANAGER", "BRANCH_MANAGER", "STORE_STAFF"
        );
        GetUsersByRoleResponse response = adminUserService.getUsersByRoles(
                baseRequest, merchantRoles, pageNumber, pageSize);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @GetMapping("/fetch-admins")
    public ResponseEntity<GetUsersByRoleResponse> fetchAdmins(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetUsersByRoleResponse response = adminUserService.getUsersByRoles(
                baseRequest, List.of("SUPER_ADMIN", "PLATFORM_ADMIN"), pageNumber, pageSize);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
