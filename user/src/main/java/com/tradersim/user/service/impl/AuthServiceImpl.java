package com.tradersim.user.service.impl;

import com.tradersim.user.dto.AuthResponse;
import com.tradersim.user.dto.LoginRequest;
import com.tradersim.user.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse authenticate(LoginRequest request) {
        // Implement authentication logic here

        return null;
    }

    @Override
    public String refreshToken(String oldToken) {
        // Implement token refresh logic here
        return null;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // Implement password change logic here
    }

    // Optionally: void initiatePasswordReset(String email);
    
} 