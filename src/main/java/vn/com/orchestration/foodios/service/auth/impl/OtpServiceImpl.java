package vn.com.orchestration.foodios.service.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.entity.auth.OtpChannel;
import vn.com.orchestration.foodios.entity.auth.OtpPurpose;
import vn.com.orchestration.foodios.entity.auth.UserOtp;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.repository.UserOtpRepository;
import vn.com.orchestration.foodios.service.auth.OtpService;
import vn.com.orchestration.foodios.service.notification.EmailMessageCommand;
import vn.com.orchestration.foodios.service.notification.EmailService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final Duration EMAIL_OTP_TTL = Duration.ofMinutes(10);
    private static final SecureRandom RNG = new SecureRandom();

    private final UserOtpRepository userOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void generateEmailVerificationOtpAndSend(User user) {
        generateEmailOtpAndSend(user, OtpPurpose.EMAIL_VERIFICATION);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resendEmailVerificationOtpRequiresNew(User user) {
        generateEmailOtpAndSend(user, OtpPurpose.EMAIL_VERIFICATION);
    }

    private void generateEmailOtpAndSend(User user, OtpPurpose purpose) {
        String otpCode = generateNumericOtp(6);

        UserOtp otp = UserOtp.builder()
                .user(user)
                .purpose(purpose)
                .channel(OtpChannel.EMAIL)
                .codeHash(passwordEncoder.encode(otpCode))
                .expiresAt(Instant.now().plus(EMAIL_OTP_TTL))
                .build();
        userOtpRepository.save(otp);

        EmailMessageCommand emailMessageCommand = EmailMessageCommand.builder()
                .toEmail(user.getEmail())
                .fullName(user.getFullName())
                .verificationCode(otpCode)
                .expireMinutes((int) EMAIL_OTP_TTL.toMinutes())
                .purpose(purpose)
                .build();

        emailService.sendEmail(emailMessageCommand);
        log.info("Generated OTP purpose={} userId={}", purpose, user.getId());
    }

    private static String generateNumericOtp(int length) {
        if (length <= 0 || length > 10) {
            throw new IllegalArgumentException("Invalid OTP length");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('0' + RNG.nextInt(10)));
        }
        return sb.toString();
    }
}
