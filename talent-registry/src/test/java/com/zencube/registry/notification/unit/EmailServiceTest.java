package com.zencube.registry.notification.unit;

import com.zencube.registry.config.service.ConfigService;
import com.zencube.registry.notification.email.impl.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendHtmlEmail_success() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Body</html>");
        when(configService.get(eq("EMAIL.FROM_ADDRESS"), eq(String.class))).thenReturn("noreply@zencube.com");

        emailService.sendTemplateEmail("test@example.com", com.zencube.registry.notification.enums.NotificationEventType.USER_REGISTERED, Map.of("key", "value"));

        verify(templateEngine).process(eq("template/test"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }
}


