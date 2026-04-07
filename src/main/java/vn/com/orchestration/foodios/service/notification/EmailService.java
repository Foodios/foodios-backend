package vn.com.orchestration.foodios.service.notification;

public interface EmailService {
    void sendEmail(EmailMessageCommand command);
}

