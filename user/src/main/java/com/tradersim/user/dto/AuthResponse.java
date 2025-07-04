package com.tradersim.user.dto;

import com.tradersim.user.model.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds until expiry
    private LocalDateTime expiresAt;
    
    // User information (without sensitive data)
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Set<RoleName> roles;
    private boolean emailVerified;
    private boolean enabled;
    
    // Authentication status
    private String message;
    private boolean success;
} 