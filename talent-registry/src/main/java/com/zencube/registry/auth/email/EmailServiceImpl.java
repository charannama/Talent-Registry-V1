package com.zencube.registry.auth.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendVerificationEmail(String to, String token) {
        log.info("--- SIMULATED EMAIL SEND ---");
        log.info("To: {}", to);
        log.info("Subject: Verify Your Talent Registry Account");
        log.info("Body:");
        log.info("Please use the following verification link to verify your account:");
        log.info("http://localhost:3000/verify-email?token={}", token);
        log.info("This link will expire in 24 hours.");
        log.info("If you did not request this, please contact support@zencube.com.");
        log.info("----------------------------");
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        log.info("--- SIMULATED EMAIL SEND ---");
        log.info("To: {}", to);
        log.info("Subject: Reset Your Talent Registry Password");
        log.info("Body:");
        log.info("We received a request to reset your password for your ZenCube Talent Registry account.");
        log.info("Click the link below to set a new password:");
        log.info("https://talent.zencube.com/reset-password?token={}", resetToken);
        log.info("This link will expire in 1 hour. If you did not request a password reset, please ignore this email.");
        log.info("For security reasons, your active sessions will be revoked when you reset your password.");
        log.info("If you need assistance, contact our support team.");
        log.info("Thank you,");
        log.info("The ZenCube Team");
        log.info("----------------------------");
    }
}
