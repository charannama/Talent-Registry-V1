package com.zencube.registry.auth.verification.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class EmailVerificationEvent extends ApplicationEvent {

    private final UUID userId;
    private final String email;
    private final String token;

    public EmailVerificationEvent(Object source, UUID userId, String email, String token) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}
