package com.beartrail.user.service;

import com.beartrail.user.dto.AuthResponse;
import com.beartrail.user.dto.LoginRequest;
import com.beartrail.user.exception.UserNotFoundException;
import com.beartrail.user.model.Role;
import com.beartrail.user.model.RoleName;
import com.beartrail.user.model.User;
import com.beartrail.user.repository.UserRepository;
import com.beartrail.user.security.JwtUtil;
import com.beartrail.user.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(RoleName.ROLE_USER);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(userRole));
        testUser.setEmailVerified(true);
        testUser.setEnabled(true);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void authenticate_Success() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(userDetails)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        when(jwtUtil.getJwtExpiration()).thenReturn(3600L);

        // When
        AuthResponse response = authService.authenticate(loginRequest);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("Authentication successful", response.getMessage());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getFirstName(), response.getFirstName());
        assertEquals(testUser.getLastName(), response.getLastName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertTrue(response.getRoles().contains(RoleName.ROLE_USER));
        assertTrue(response.isEmailVerified());
        assertTrue(response.isEnabled());
    }

    @Test
    void authenticate_InvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When
        AuthResponse response = authService.authenticate(loginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Authentication failed: Bad credentials", response.getMessage());
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
    }

    @Test
    void authenticate_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When
        AuthResponse response = authService.authenticate(loginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Authentication error"));
        assertNull(response.getAccessToken());
    }

    @Test
    void authenticate_GeneralException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When
        AuthResponse response = authService.authenticate(loginRequest);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("Authentication error: Unexpected error", response.getMessage());
        assertNull(response.getAccessToken());
    }

    @Test
    void refreshToken_Success() {
        // Given
        String oldToken = "old-token";
        String username = "john.doe@example.com";
        String newToken = "new-token";

        when(jwtUtil.extractUsername(oldToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(oldToken, userDetails)).thenReturn(true);
        when(jwtUtil.generateToken(userDetails)).thenReturn(newToken);

        // When
        String result = authService.refreshToken(oldToken);

        // Then
        assertEquals(newToken, result);
    }

    @Test
    void refreshToken_InvalidToken() {
        // Given
        String oldToken = "invalid-token";
        String username = "john.doe@example.com";

        when(jwtUtil.extractUsername(oldToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.validateToken(oldToken, userDetails)).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(oldToken));
    }

    @Test
    void refreshToken_ExtractionFailure() {
        // Given
        String oldToken = "malformed-token";
        when(jwtUtil.extractUsername(oldToken)).thenThrow(new RuntimeException("Token malformed"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(oldToken));
    }

    @Test
    void changePassword_Success() {
        // Given
        Long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // When
        assertDoesNotThrow(() -> authService.changePassword(userId, oldPassword, newPassword));

        // Then
        verify(userRepository).save(testUser);
        assertEquals(encodedNewPassword, testUser.getPassword());
    }

    @Test
    void changePassword_UserNotFound() {
        // Given
        Long userId = 999L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> authService.changePassword(userId, oldPassword, newPassword));
    }

    @Test
    void changePassword_IncorrectOldPassword() {
        // Given
        Long userId = 1L;
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, 
                () -> authService.changePassword(userId, oldPassword, newPassword));
    }
}
