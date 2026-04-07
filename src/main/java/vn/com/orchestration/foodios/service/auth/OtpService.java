package vn.com.orchestration.foodios.service.auth;

import vn.com.orchestration.foodios.entity.user.User;

public interface OtpService {
    void generateEmailVerificationOtpAndSend(User user);

    void resendEmailVerificationOtpRequiresNew(User user);
}

