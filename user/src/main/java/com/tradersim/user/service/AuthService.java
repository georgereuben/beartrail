package com.tradersim.user.service;

import com.tradersim.user.dto.AuthResponse;
import com.tradersim.user.dto.LoginRequest;

public interface AuthService {
    AuthResponse authenticate(LoginRequest request);
    String refreshToken(String oldToken);
    void changePassword(Long userId, String oldPassword, String newPassword);
    // Optionally: void initiatePasswordReset(String email);
}