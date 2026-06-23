package com.zencube.registry.passwordreset.service;

public interface PasswordResetService {
    void requestReset(String email);
    void confirmReset(String token, String newPassword);
}
