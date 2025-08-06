package com.beartrail.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.beartrail.user.TestConstants;
import com.beartrail.user.dto.AuthResponse;
import com.beartrail.user.dto.LoginRequest;
import com.beartrail.user.dto.UserRegistrationRequest;
import com.beartrail.user.model.Role;
import com.beartrail.user.model.RoleName;
import com.beartrail.user.model.User;
import com.beartrail.user.repository.RoleRepository;
import com.beartrail.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthFlowIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private UserRegistrationRequest registrationRequest;
    private LoginRequest loginRequest;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Setup MockMvc manually
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clean up database
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create user role
        userRole = new Role();
        userRole.setName(RoleName.ROLE_USER);
        roleRepository.save(userRole);

        // Prepare test data
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFirstName(TestConstants.TEST_FIRST_NAME);
        registrationRequest.setLastName(TestConstants.TEST_LAST_NAME);
        registrationRequest.setEmail(TestConstants.TEST_EMAIL);
        registrationRequest.setPassword(TestConstants.TEST_PASSWORD);

        loginRequest = new LoginRequest();
        loginRequest.setEmail(TestConstants.TEST_EMAIL);
        loginRequest.setPassword(TestConstants.TEST_PASSWORD);
    }

    @Test
    void completeAuthFlow_RegisterThenLogin() throws Exception {
        // Step 1: Register a new user
        mockMvc.perform(post(TestConstants.REGISTER_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TestConstants.JSON_SUCCESS).value(true))
                .andExpect(jsonPath(TestConstants.JSON_MESSAGE).value(TestConstants.REGISTRATION_SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.firstName").value(TestConstants.TEST_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(TestConstants.TEST_LAST_NAME))
                .andExpect(jsonPath("$.email").value(TestConstants.TEST_EMAIL));

        // Force database synchronization after registration
        entityManager.flush();
        entityManager.clear();

        // Verify user exists in database
        assertTrue(userRepository.findByEmail(TestConstants.TEST_EMAIL).isPresent());

        // Step 2: Login with the registered user
        mockMvc.perform(post(TestConstants.LOGIN_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(TestConstants.JSON_SUCCESS).value(true))
                .andExpect(jsonPath(TestConstants.JSON_MESSAGE).value(TestConstants.AUTH_SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.firstName").value(TestConstants.TEST_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(TestConstants.TEST_LAST_NAME))
                .andExpect(jsonPath("$.email").value(TestConstants.TEST_EMAIL))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void registerUser_DuplicateEmail_ShouldFail() throws Exception {
        // Step 1: Register first user
        mockMvc.perform(post(TestConstants.REGISTER_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        // Step 2: Try to register with same email
        mockMvc.perform(post(TestConstants.REGISTER_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath(TestConstants.JSON_SUCCESS).value(false))
                .andExpect(jsonPath(TestConstants.JSON_MESSAGE).value("Registration failed: Email already in use"));

        // Verify only one user exists
        assertEquals(1, userRepository.count());
    }

    @Test
    void loginWithWrongCredentials_ShouldFail() throws Exception {
        // First register a user
        createTestUser();

        // Try to login with wrong password
        LoginRequest wrongPasswordRequest = new LoginRequest();
        wrongPasswordRequest.setEmail(TestConstants.TEST_EMAIL);
        wrongPasswordRequest.setPassword("wrongPassword");
        mockMvc.perform(post(TestConstants.LOGIN_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath(TestConstants.JSON_SUCCESS).value(false))
                .andExpect(jsonPath(TestConstants.JSON_MESSAGE).exists())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void loginWithNonExistentUser_ShouldFail() throws Exception {
        LoginRequest nonExistentUserRequest = new LoginRequest();
        nonExistentUserRequest.setEmail("nonexistent@example.com");
        nonExistentUserRequest.setPassword(TestConstants.TEST_PASSWORD);
        mockMvc.perform(post(TestConstants.LOGIN_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentUserRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath(TestConstants.JSON_SUCCESS).value(false))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void registerWithInvalidData_ShouldFail() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest();
        invalidRequest.setFirstName(""); // Empty first name
        invalidRequest.setLastName(TestConstants.TEST_LAST_NAME);
        invalidRequest.setEmail("invalid-email"); // Invalid email format
        invalidRequest.setPassword(""); // Empty password

        mockMvc.perform(post(TestConstants.REGISTER_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify no user was created
        assertEquals(0, userRepository.count());
    }

    @Test
    void userRolesAreAssignedCorrectly() throws Exception {
        // Register user
        mockMvc.perform(post(TestConstants.REGISTER_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        // Verify user has correct role
        User savedUser = userRepository.findByEmail(TestConstants.TEST_EMAIL).orElseThrow();
        assertNotNull(savedUser.getRoles());
        assertEquals(1, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_USER));
    }

    @Test
    void passwordIsProperlyEncoded() throws Exception {
        // Register user
        mockMvc.perform(post(TestConstants.REGISTER_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        // Verify password is encoded
        User savedUser = userRepository.findByEmail(TestConstants.TEST_EMAIL).orElseThrow();
        assertNotEquals("password123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void tokenIsValidAfterSuccessfulLogin() throws Exception {
        // Register user first
        createTestUser();

        // Force database synchronization after user creation
        entityManager.flush();
        entityManager.clear();

        // Login and extract token
        MvcResult loginResult = mockMvc.perform(post(TestConstants.LOGIN_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseContent, AuthResponse.class);

        assertNotNull(authResponse.getAccessToken());
        assertNotNull(authResponse.getRefreshToken());
        assertTrue(authResponse.getExpiresIn() > 0);
        assertNotNull(authResponse.getExpiresAt());
    }

    private void createTestUser() {
        // Check if user already exists, if so delete it first
        userRepository.findByEmail(TestConstants.TEST_EMAIL).ifPresent(userRepository::delete);

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail(TestConstants.TEST_EMAIL);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEnabled(true);
        user.setLocked(false);
        user.setCredentialsExpired(false);
        user.setEmailVerified(false);
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        // Force database synchronization
        entityManager.flush();
        entityManager.clear();
    }
}
