package com.zencube.registry.notification.email.impl;

import com.zencube.registry.notification.email.EmailTemplateService;
import com.zencube.registry.notification.email.exception.EmailTemplateException;
import com.zencube.registry.notification.enums.NotificationEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public String generateSubject(NotificationEventType eventType, Map<String, Object> variables) {
        if (variables.containsKey("subject")) {
            return (String) variables.get("subject");
        }

        return switch (eventType) {
            case USER_REGISTERED -> "Welcome to ZenCube Talent Registry!";
            case EMAIL_VERIFIED -> "Your email has been verified";
            case PASSWORD_RESET -> "Password Reset Request";
            case ENTERPRISE_REGISTERED -> "Enterprise Registration Received";
            case ENTERPRISE_APPROVED -> "Your Enterprise has been Approved";
            case ENTERPRISE_REJECTED -> "Update on your Enterprise Registration";
            case ENTERPRISE_SUSPENDED -> "Enterprise Account Suspended";
            case OPENING_CREATED -> "New Opening Created";
            case OPENING_APPROVED -> "Your Opening has been Approved";
            case OPENING_REJECTED -> "Update on your Opening";
            case APPLICATION_SUBMITTED -> "Application Successfully Submitted";
            case APPLICATION_REVIEWED -> "Your Application is Under Review";
            case APPLICATION_FORWARDED -> "Your Application has been Forwarded";
            case INTERVIEW_SCHEDULED -> "Interview Scheduled";
            case APPLICATION_SELECTED -> "Congratulations! You have been Selected";
            case SUCCESS_STORY_CREATED -> "New Success Story Published";
            case FORMAL_REQUEST_CREATED -> "Formal Request Received";
            default -> "Notification from ZenCube Talent Registry";
        };
    }

    @Override
    public String generateBody(NotificationEventType eventType, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }

            String templateName = getTemplateName(eventType);
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            log.error("Failed to render email template for event {}", eventType, e);
            throw new EmailTemplateException("Failed to render email template for event " + eventType, e);
        }
    }

    private String getTemplateName(NotificationEventType eventType) {
        return switch (eventType) {
            case EMAIL_VERIFIED, USER_REGISTERED -> "email/verify-email";
            case PASSWORD_RESET -> "email/password-reset";
            case ENTERPRISE_APPROVED, ENTERPRISE_REJECTED, ENTERPRISE_SUSPENDED -> "email/enterprise-status";
            case APPLICATION_SUBMITTED, APPLICATION_FORWARDED, APPLICATION_SELECTED -> "email/application-status";
            case INTERVIEW_SCHEDULED -> "email/application-status";
            case SUCCESS_STORY_CREATED -> "email/system-notification";
            default -> "email/system-notification";
        };
    }
}
