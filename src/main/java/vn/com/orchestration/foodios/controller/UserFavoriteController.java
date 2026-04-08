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
import vn.com.orchestration.foodios.dto.user.GetFavoriteMerchantsResponse;
import vn.com.orchestration.foodios.dto.user.ToggleFavoriteRequest;
import vn.com.orchestration.foodios.dto.user.ToggleFavoriteResponse;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.user.UserFavoriteService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;
import static vn.com.orchestration.foodios.constant.ApiConstant.USERS_PATH;

@RestController
@RequestMapping(API_PATH + API_VERSION + USERS_PATH + "/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping("/toggle")
    public ResponseEntity<ToggleFavoriteResponse> toggleFavorite(@Valid @RequestBody ToggleFavoriteRequest request) {
        ToggleFavoriteResponse response = userFavoriteService.toggleFavorite(request);
        return HttpUtils.buildResponse(request, response);
    }

    @GetMapping
    public ResponseEntity<GetFavoriteMerchantsResponse> getFavorites(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetFavoriteMerchantsResponse response = userFavoriteService.getFavorites(baseRequest, pageNumber, pageSize);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
