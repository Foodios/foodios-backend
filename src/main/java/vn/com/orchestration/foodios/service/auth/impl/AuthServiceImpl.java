package vn.com.orchestration.foodios.service.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.auth.LoginRequest;
import vn.com.orchestration.foodios.dto.auth.LoginResponse;
import vn.com.orchestration.foodios.dto.auth.RegisterRequest;
import vn.com.orchestration.foodios.dto.auth.RegisterResponse;
import vn.com.orchestration.foodios.dto.auth.VerifyEmailOtpRequest;
import vn.com.orchestration.foodios.dto.auth.VerifyEmailOtpResponse;
import vn.com.orchestration.foodios.dto.auth.VerifyEmailResponseData;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.entity.auth.OtpPurpose;
import vn.com.orchestration.foodios.entity.auth.UserOtp;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.entity.user.UserStatus;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.JwtService;
import vn.com.orchestration.foodios.repository.UserOtpRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.auth.AuthService;
import vn.com.orchestration.foodios.service.auth.OtpService;
import vn.com.orchestration.foodios.service.auth.RefreshTokenService;
import vn.com.orchestration.foodios.service.auth.UserAuthorizationService;
import vn.com.orchestration.foodios.service.auth.UserRoleService;
import vn.com.orchestration.foodios.service.loyalty.CustomerMembershipService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import static vn.com.orchestration.foodios.constant.ErrorConstant.DUPLICATE_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.EMAIL_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.EMAIL_NOT_VERIFIED_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_INPUT_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_OTP_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.INVALID_USERNAME_OR_PASSWORD_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.OTP_EXPIRED_OR_NOT_FOUND_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.PHONE_NUMBER_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.RECORD_NOT_FOUND;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_CODE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SUCCESS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.SYSTEM_ERROR;
import static vn.com.orchestration.foodios.constant.ErrorConstant.TOO_MANY_ATTEMPTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USERNAME_EXISTS_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_ACTIVE_MESSAGE;
import static vn.com.orchestration.foodios.constant.ErrorConstant.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserOtpRepository userOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final UserAuthorizationService userAuthorizationService;
    private final UserRoleService userRoleService;
    private final CustomerMembershipService customerMembershipService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info(
                "[REGISTER] requestId={}, username={}, email={}, phone={}",
                request.getRequestId(),
                request.getData() != null ? request.getData().getUsername() : null,
                request.getData() != null ? request.getData().getEmail() : null,
                request.getData() != null ? request.getData().getPhone() : null
        );

        RegisterResponse response = new RegisterResponse();
        RegisterRequest.RegisterRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        String username = data.getUsername().trim();
        String email = data.getEmail().trim().toLowerCase(Locale.ROOT);
        String phone = data.getPhone().trim();

        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User existingUser = existingByEmail.get();
            if (UserStatus.VERIFYING.equals(existingUser.getStatus())) {
                try {
                    otpService.resendEmailVerificationOtpRequiresNew(existingUser);
                } catch (Exception e) {
                    log.info("[REGISTER] Resend verify email failed for VERIFYING userId={}", existingUser.getId(), e);
                }
            }
            throw businessException(request, DUPLICATE_ERROR, EMAIL_EXISTS_MESSAGE);
        }

        if (userRepository.existsByPhone(phone)) {
            throw businessException(request, DUPLICATE_ERROR, PHONE_NUMBER_EXISTS_MESSAGE);
        }

        if (userRepository.existsByUsername(username)) {
            throw businessException(request, DUPLICATE_ERROR, USERNAME_EXISTS_MESSAGE);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(data.getPassword()))
                .status(UserStatus.VERIFYING)
                .build();
        // Force INSERT early so DB constraint/type issues fail before we send OTP email.
        userRepository.saveAndFlush(user);

        // Default role + membership/points for a new customer
        try {
            userRoleService.assignDefaultCustomerRole(user);
            customerMembershipService.createForNewCustomer(user);
        } catch (Exception e) {
            throw businessException(request, SYSTEM_ERROR, e.getMessage());
        }

        try {
            otpService.generateEmailVerificationOtpAndSend(user);
        } catch (Exception e) {
            throw businessException(request, SYSTEM_ERROR, e.getMessage());
        }

        response.setData(
                RegisterResponse.RegisterResponseData.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .status(user.getStatus())
                        .build()
        );
        response.setResult(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info(
                "[LOGIN] requestId={}, identifier={}",
                request.getRequestId(),
                request.getData() != null ? request.getData().getIdentifier() : null
        );

        LoginResponse response = new LoginResponse();
        LoginRequest.LoginRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        String identifier = data.getIdentifier().trim();

        User user =
                userRepository.findByUsername(identifier)
                        .orElseGet(() ->
                                userRepository.findByEmail(identifier.toLowerCase(Locale.ROOT))
                                        .orElseGet(() -> userRepository.findByPhone(identifier).orElse(null)));

        if (user == null || !passwordEncoder.matches(data.getPassword(), user.getPasswordHash())) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_USERNAME_OR_PASSWORD_MESSAGE);
        }

        if (user.getStatus() == UserStatus.VERIFYING) {
            throw businessException(request, INVALID_INPUT_ERROR, EMAIL_NOT_VERIFIED_MESSAGE);
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw businessException(request, INVALID_INPUT_ERROR, USER_NOT_ACTIVE_MESSAGE);
        }

        java.util.Set<String> roles = userAuthorizationService.getRoles(user);
        java.util.Set<String> authorities = userAuthorizationService.getAuthorities(user);
        String accessToken = jwtService.generateAccessToken(user, roles, authorities);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenService.saveNew(user, refreshToken);

        response.setData(
                LoginResponse.LoginResponseData.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .roles(roles)
                        .authorities(authorities)
                        .profileCompleted(user.isProfileCompleted())
                        .build()
        );
        response.setResult(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build());
        return response;
    }

    @Override
    @Transactional
    public VerifyEmailOtpResponse verifyEmailOtp(VerifyEmailOtpRequest request) {
        log.info(
                "[VERIFY_EMAIL] requestId={}, email={}",
                request.getRequestId(),
                request.getData() != null ? request.getData().getEmail() : null
        );

        VerifyEmailOtpResponse response = new VerifyEmailOtpResponse();
        VerifyEmailOtpRequest.VerifyEmailOtpRequestData data = request.getData();
        if (data == null) {
            throw businessException(request, INVALID_INPUT_ERROR, "Missing data");
        }

        String email = data.getEmail().trim().toLowerCase(Locale.ROOT);
        String code = data.getCode().trim();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, USER_NOT_FOUND_MESSAGE));

        if (user.getStatus() == UserStatus.ACTIVE) {
            response.setData(VerifyEmailResponseData.builder().verified(true).build());
            response.setResult(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build());
            return response;
        }
        if (user.getStatus() != UserStatus.VERIFYING) {
            throw businessException(request, INVALID_INPUT_ERROR, USER_NOT_ACTIVE_MESSAGE);
        }

        UserOtp otp =
                userOtpRepository
                        .findFirstByUserIdAndPurposeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                                user.getId(), OtpPurpose.EMAIL_VERIFICATION, Instant.now())
                        .orElse(null);

        if (otp == null) {
            throw businessException(request, INVALID_INPUT_ERROR, OTP_EXPIRED_OR_NOT_FOUND_MESSAGE);
        }

        if (otp.getAttempts() >= otp.getMaxAttempts()) {
            throw businessException(request, INVALID_INPUT_ERROR, TOO_MANY_ATTEMPTS_MESSAGE);
        }

        otp.setAttempts(otp.getAttempts() + 1);
        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            userOtpRepository.save(otp);
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_OTP_MESSAGE);
        }

        otp.setUsedAt(Instant.now());
        userOtpRepository.save(otp);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        response.setData(VerifyEmailResponseData.builder().verified(true).build());
        response.setResult(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build());
        return response;
    }

    private static BusinessException businessException(
            vn.com.orchestration.foodios.dto.common.BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message));
    }
}
