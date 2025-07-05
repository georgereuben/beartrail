package com.beartrail.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.beartrail.user.dto.AuthResponse;
import com.beartrail.user.dto.LoginRequest;
import com.beartrail.user.dto.UserRegistrationRequest;
import com.beartrail.user.exception.UserAlreadyExistsException;
import com.beartrail.user.model.RoleName;
import com.beartrail.user.model.User;
import com.beartrail.user.security.JwtUtil;
import com.beartrail.user.service.AuthService;
import com.beartrail.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean                           // TODO: Replace @MockBean usage (deprecated in Spring 6)
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private LoginRequest validLoginRequest;
    private UserRegistrationRequest validRegistrationRequest;
    private AuthResponse successAuthResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john.doe@example.com");
        validLoginRequest.setPassword("password123");

        validRegistrationRequest = new UserRegistrationRequest();
        validRegistrationRequest.setFirstName("John");
        validRegistrationRequest.setLastName("Doe");
        validRegistrationRequest.setEmail("john.doe@example.com");
        validRegistrationRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setEnabled(true);
        testUser.setEmailVerified(true);

        successAuthResponse = AuthResponse.builder()
                .success(true)
                .message("Authentication successful")
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .expiresAt(LocalDateTime.now().plusSeconds(3600))
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .roles(Set.of(RoleName.ROLE_USER))
                .emailVerified(true)
                .enabled(true)
                .build();
    }

    @Test
    @WithMockUser
    void login_Success() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(successAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication successful"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser
    void login_InvalidCredentials() throws Exception {
        // Given
        AuthResponse errorResponse = AuthResponse.builder()
                .success(false)
                .message("Authentication failed: Bad credentials")
                .build();

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(errorResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication failed: Bad credentials"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    @WithMockUser
    void login_ServiceThrowsException() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Service unavailable"));
    }

    @Test
    @WithMockUser
    void login_EmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void register_Success() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser
    void register_UserAlreadyExists() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists with email: john.doe@example.com"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Registration failed: User already exists with email: john.doe@example.com"));
    }

    @Test
    @WithMockUser
    void register_ServiceReturnsEmpty() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser
    void register_GeneralException() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Registration failed: Database connection failed"));
    }

    @Test
    @WithMockUser
    void register_InvalidRequestData() throws Exception {
        // Given
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
        invalidRequest.setFirstName(""); // Empty first name should fail validation
        invalidRequest.setLastName("Doe");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void register_MissingRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
