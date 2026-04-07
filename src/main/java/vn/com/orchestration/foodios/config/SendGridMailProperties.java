package vn.com.orchestration.foodios.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "notification.email.sendgrid")
public class SendGridMailProperties {
    private String baseUrl;
    private String apiKey;
    private String fromEmail;
    private String fromName;
    private String verifySubject;
}

