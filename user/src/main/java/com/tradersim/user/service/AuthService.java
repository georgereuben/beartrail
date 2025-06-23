package com.tradersim.user.service;

import com.tradersim.user.dto.AuthResponse;
import com.tradersim.user.dto.LoginRequest;
import com.tradersim.user.exception.UserNotFoundException;

public interface AuthService {
    AuthResponse authenticate(LoginRequest request);
    String refreshToken(String oldToken);
    void changePassword(Long userId, String oldPassword, String newPassword) throws UserNotFoundException;
    // Optionally: void initiatePasswordReset(String email);
}