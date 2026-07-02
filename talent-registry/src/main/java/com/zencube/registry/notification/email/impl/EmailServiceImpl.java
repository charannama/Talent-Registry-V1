package com.zencube.registry.notification.email.impl;

import com.zencube.registry.notification.email.EmailService;
import com.zencube.registry.notification.email.EmailTemplateService;
import com.zencube.registry.notification.email.exception.EmailDeliveryException;
import com.zencube.registry.notification.enums.NotificationEventType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username:noreply@zencube.com}")
    private String fromEmail;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public void sendEmail(String to, String subject, String body, Boolean isHtml) {
        if (!validateRecipient(to)) {
            log.error("Invalid email address: {}", to);
            throw new EmailDeliveryException("Invalid email address: " + to);
        }

        try {
            boolean useHtml = isHtml != null ? isHtml : true;
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, useHtml);
            
            javaMailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            throw new EmailDeliveryException("Email delivery failed to " + to, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}", to, e);
            throw new EmailDeliveryException("Unexpected email delivery error", e);
        }
    }

    @Override
    public void sendTemplateEmail(String to, NotificationEventType eventType, Map<String, Object> variables) {
        String subject = emailTemplateService.generateSubject(eventType, variables);
        String body = emailTemplateService.generateBody(eventType, variables);
        sendEmail(to, subject, body, true);
    }

    @Override
    public boolean validateRecipient(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
