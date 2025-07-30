package com.beartrail.user.service;

import com.beartrail.user.dto.AuthResponse;
import com.beartrail.user.dto.LoginRequest;
import com.beartrail.user.exception.UserNotFoundException;

public interface AuthService {
    AuthResponse authenticate(LoginRequest request);
    String refreshToken(String oldToken);
    void changePassword(Long userId, String oldPassword, String newPassword) throws UserNotFoundException;
    // Optionally: void initiatePasswordReset(String email);
}