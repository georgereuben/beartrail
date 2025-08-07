package com.beartrail.user.dto;

import com.beartrail.user.model.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor               // using all argscontructor was failing spotbugs
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

    // Defensive copy in constructor
    public AuthResponse(String accessToken, String refreshToken, String tokenType, Long expiresIn, LocalDateTime expiresAt, Long userId, String firstName, String lastName, String email, Set<RoleName> roles, boolean emailVerified, boolean enabled, String message, boolean success) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.expiresAt = expiresAt;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles == null ? null : new java.util.HashSet<>(roles);
        this.emailVerified = emailVerified;
        this.enabled = enabled;
        this.message = message;
        this.success = success;
    }

    // Defensive copy in setter
    public void setRoles(Set<RoleName> roles) {
        this.roles = roles == null ? null : new java.util.HashSet<>(roles);
    }

    // Unmodifiable view in getter
    public Set<RoleName> getRoles() {
        return roles == null ? null : java.util.Collections.unmodifiableSet(roles);
    }
}
