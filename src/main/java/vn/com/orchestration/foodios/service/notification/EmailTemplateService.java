package vn.com.orchestration.foodios.service.notification;

import java.util.Map;

public interface EmailTemplateService {
    String processTemplate(String templateName, Map<String, Object> variables);
}

