package vn.com.orchestration.foodios.service.auth;

import vn.com.orchestration.foodios.dto.auth.LoginRequest;
import vn.com.orchestration.foodios.dto.auth.LoginResponse;
import vn.com.orchestration.foodios.dto.auth.LogoutRequest;
import vn.com.orchestration.foodios.dto.auth.LogoutResponse;
import vn.com.orchestration.foodios.dto.auth.RefreshTokenRequest;
import vn.com.orchestration.foodios.dto.auth.RefreshTokenResponse;
import vn.com.orchestration.foodios.dto.auth.RegisterRequest;
import vn.com.orchestration.foodios.dto.auth.RegisterResponse;
import vn.com.orchestration.foodios.dto.auth.VerifyEmailOtpRequest;
import vn.com.orchestration.foodios.dto.auth.VerifyEmailOtpResponse;

public interface AuthenticationService {
    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    VerifyEmailOtpResponse verifyEmailOtp(VerifyEmailOtpRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    LogoutResponse logout(LogoutRequest request);
}
