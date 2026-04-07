package vn.com.orchestration.foodios.service.notification;

import lombok.Builder;
import vn.com.orchestration.foodios.entity.auth.OtpPurpose;

@Builder
public record EmailMessageCommand(
        String toEmail,
        String fullName,
        String verificationCode,
        int expireMinutes,
        OtpPurpose purpose
) {}

