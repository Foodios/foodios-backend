package vn.com.orchestration.foodios.service.notification.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import vn.com.orchestration.foodios.config.SendGridMailProperties;
import vn.com.orchestration.foodios.entity.auth.OtpPurpose;
import vn.com.orchestration.foodios.service.notification.EmailMessageCommand;
import vn.com.orchestration.foodios.service.notification.EmailService;
import vn.com.orchestration.foodios.service.notification.EmailTemplateService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.email", name = "provider", havingValue = "sendgrid")
public class SendGridEmailServiceImpl implements EmailService {

    private static final URI SENDGRID_SEND_URI = URI.create("https://api.sendgrid.com/v3/mail/send");

    private final EmailTemplateService emailTemplateService;
    private final SendGridMailProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendEmail(EmailMessageCommand command) {
        Map<String, Object> variables = getTemplateVariables(command);

        String templateName;
        if (OtpPurpose.FORGOT_PASSWORD.equals(command.purpose())) {
            templateName = "email/forgot-password";
        } else {
            templateName = "email/verification-code";
        }

        String htmlBody = emailTemplateService.processTemplate(templateName, variables);
        String payload = buildSendGridPayload(command.toEmail(), properties.getVerifySubject(), htmlBody);

        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY is missing");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(SENDGRID_SEND_URI)
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Sending Email Response: status={}, body={}", response.statusCode(), response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "SendGrid send mail failed. Status=%s, body=%s"
                                .formatted(response.statusCode(), response.body())
                );
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to send email via SendGrid", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to send email via SendGrid", ex);
        }
    }

    private Map<String, Object> getTemplateVariables(EmailMessageCommand command) {
        Map<String, Object> variables = new HashMap<>();
        String fullName = command.fullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = "bạn";
        }
        variables.put("fullName", fullName);
        variables.put("otpCode", command.verificationCode());
        variables.put("expiredMinutes", command.expireMinutes());
        variables.put("baseUrl", properties.getBaseUrl());
        return variables;
    }

    private String buildSendGridPayload(String toEmail, String subject, String htmlBody) {
        if (subject == null || subject.isBlank()) {
            subject = "Verify your email";
        }

        String fromEmail = properties.getFromEmail();
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("SENDGRID_FROM_EMAIL is missing");
        }

        String fromName = properties.getFromName();
        if (fromName == null) {
            fromName = "";
        }

        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode personalizations = root.putArray("personalizations");
        ObjectNode personalization = objectMapper.createObjectNode();
        ArrayNode to = personalization.putArray("to");
        ObjectNode toObj = objectMapper.createObjectNode();
        toObj.put("email", toEmail);
        to.add(toObj);
        personalization.put("subject", subject);
        personalizations.add(personalization);

        ObjectNode from = root.putObject("from");
        from.put("email", fromEmail);
        if (!fromName.isBlank()) {
            from.put("name", fromName);
        }

        ArrayNode content = root.putArray("content");
        ObjectNode html = objectMapper.createObjectNode();
        html.put("type", "text/html");
        html.put("value", htmlBody);
        content.add(html);

        return root.toString();
    }
}

