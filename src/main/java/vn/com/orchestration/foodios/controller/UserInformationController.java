package vn.com.orchestration.foodios.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.user.GetMyProfileResponse;
import vn.com.orchestration.foodios.dto.user.UpdateProfileRequest;
import vn.com.orchestration.foodios.dto.user.UpdateProfileResponse;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.log.SystemLog;
import vn.com.orchestration.foodios.service.user.UserInformationService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.ME_PROFILE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.PROFILE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.UPDATE_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.USERS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + USERS_PATH)
@RequiredArgsConstructor
public class UserInformationController {

    private final UserInformationService userInformationService;
    private final SystemLog sLog = SystemLog.getLogger(this.getClass());

    @GetMapping(ME_PROFILE_PATH)
    public ResponseEntity<GetMyProfileResponse> getMyProfile(
            HttpServletRequest request,
            @RequestParam String userId) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);

        GetMyProfileResponse response = userInformationService.getMyProfile(baseRequest, userId);
        return HttpUtils.buildResponse(baseRequest, response);
    }


    @PostMapping(PROFILE_PATH + UPDATE_PATH)
    public ResponseEntity<UpdateProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        sLog.info("[USER-PROFILE] Update Profile Request: {}", request);
        UpdateProfileResponse response = userInformationService.updateProfile(request);
        sLog.info("[USER-PROFILE] Update Profile Response: {}", response);
        return HttpUtils.buildResponse(request, response);
    }
}
