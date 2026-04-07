package vn.com.orchestration.foodios.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.auth.RegisterResponse;
import vn.com.orchestration.foodios.dto.auth.LoginResponse;
import vn.com.orchestration.foodios.dto.auth.LoginRequest;
import vn.com.orchestration.foodios.dto.auth.RegisterRequest;
import vn.com.orchestration.foodios.dto.auth.VerifyEmailOtpRequest;
import vn.com.orchestration.foodios.dto.auth.VerifyEmailOtpResponse;
import vn.com.orchestration.foodios.service.auth.AuthService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.AUTHENTICATION_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.LOGIN_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_PREFIX;
import static vn.com.orchestration.foodios.constant.ApiConstant.REGISTER_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.VERIFY_EMAIL_PATH;

@RestController
@RequestMapping(API_PREFIX + AUTHENTICATION_PATH)
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping(REGISTER_PATH)
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    RegisterResponse response = authService.register(request);
    return HttpUtils.buildResponse(request, response);
  }

  @PostMapping(VERIFY_EMAIL_PATH)
  public ResponseEntity<VerifyEmailOtpResponse> verifyEmail(
      @Valid @RequestBody VerifyEmailOtpRequest request) {
    VerifyEmailOtpResponse response = authService.verifyEmailOtp(request);
    return HttpUtils.buildResponse(request, response);
  }

  @PostMapping(LOGIN_PATH)
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return HttpUtils.buildResponse(request, response);
  }
}
