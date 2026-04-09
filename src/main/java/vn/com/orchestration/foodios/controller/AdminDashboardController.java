package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.admin.GlobalDashboardResponse;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.admin.AdminDashboardService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.ADMIN_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;

@RestController
@RequestMapping(API_PATH + API_VERSION + ADMIN_PATH + "/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<GlobalDashboardResponse> getGlobalDashboard(HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GlobalDashboardResponse response = adminDashboardService.getGlobalDashboardData(baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
