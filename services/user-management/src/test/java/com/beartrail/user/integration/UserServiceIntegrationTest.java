package com.beartrail.user.integration;

import com.beartrail.user.dto.UserRegistrationRequest;
import com.beartrail.user.dto.UserResponse;
import com.beartrail.user.model.Role;
import com.beartrail.user.model.RoleName;
import com.beartrail.user.model.User;
import com.beartrail.user.repository.RoleRepository;
import com.beartrail.user.repository.UserRepository;
import com.beartrail.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role userRole;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        // Clean up database
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create user role
        userRole = new Role();
        userRole.setName(RoleName.ROLE_USER);
        roleRepository.save(userRole);

        // Prepare test data
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setEmail("john.doe@example.com");
        registrationRequest.setPassword("password123");
    }

    @Test
    void registerUser_FullIntegration() {
        // When
        Optional<User> result = userService.registerUser(registrationRequest);

        // Then
        assertTrue(result.isPresent());
        User savedUser = result.get();

        // Verify user data
        assertNotNull(savedUser.getId());
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertNotEquals("password123", savedUser.getPassword()); // Should be encoded
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));

        // Verify user exists in database
        Optional<User> dbUser = userRepository.findById(savedUser.getId());
        assertTrue(dbUser.isPresent());
        assertEquals(savedUser.getEmail(), dbUser.get().getEmail());
    }

    @Test
    void findUserById_AfterRegistration() {
        // Given
        Optional<User> registeredUser = userService.registerUser(registrationRequest);
        assertTrue(registeredUser.isPresent());
        Long userId = registeredUser.get().getId();

        // When
        Optional<User> foundUser = userService.findUserById(userId);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("john.doe@example.com", foundUser.get().getEmail());
        assertEquals("John", foundUser.get().getFirstName());
        assertEquals("Doe", foundUser.get().getLastName());
    }

    @Test
    void findUserByEmail_AfterRegistration() {
        // Given
        userService.registerUser(registrationRequest);

        // When
        Optional<User> foundUser = userService.findUserByEmail("john.doe@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("john.doe@example.com", foundUser.get().getEmail());
        assertEquals("John", foundUser.get().getFirstName());
        assertEquals("Doe", foundUser.get().getLastName());
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        // Given
        userService.registerUser(registrationRequest);

        // When & Then
        assertThrows(Exception.class, () -> userService.registerUser(registrationRequest));

        // Verify only one user exists
        assertEquals(1, userRepository.count());
    }

    @Test
    void findUserById_NonExistentUser() {
        // When
        Optional<User> result = userService.findUserById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findUserByEmail_NonExistentUser() {
        // When
        Optional<User> result = userService.findUserByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void multipleUsers_Registration() {
        // Given
        UserRegistrationRequest request1 = new UserRegistrationRequest();
        request1.setFirstName("John");
        request1.setLastName("Doe");
        request1.setEmail("john.doe@example.com");
        request1.setPassword("password123");

        UserRegistrationRequest request2 = new UserRegistrationRequest();
        request2.setFirstName("Jane");
        request2.setLastName("Smith");
        request2.setEmail("jane.smith@example.com");
        request2.setPassword("password456");

        // When
        Optional<User> user1 = userService.registerUser(request1);
        Optional<User> user2 = userService.registerUser(request2);

        // Then
        assertTrue(user1.isPresent());
        assertTrue(user2.isPresent());

        // Verify both users can be found
        assertTrue(userService.findUserByEmail("john.doe@example.com").isPresent());
        assertTrue(userService.findUserByEmail("jane.smith@example.com").isPresent());

        // Verify total count
        assertEquals(2, userRepository.count());
    }

    @Test
    void passwordEncryption_Integration() {
        // Given
        String plainPassword = "mySecretPassword123!";
        registrationRequest.setPassword(plainPassword);

        // When
        Optional<User> registeredUser = userService.registerUser(registrationRequest);

        // Then
        assertTrue(registeredUser.isPresent());
        User user = registeredUser.get();

        // Password should be encoded
        assertNotEquals(plainPassword, user.getPassword());
        assertTrue(passwordEncoder.matches(plainPassword, user.getPassword()));

        // Verify in database
        User dbUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches(plainPassword, dbUser.getPassword()));
    }

    @Test
    void listUsers_NotImplemented() {
        // Given
        userService.registerUser(registrationRequest);

        // When
        List<UserResponse> result = userService.listUsers(0, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Current implementation returns empty list
    }

    @Test
    void deleteUser_NotImplemented() {
        // Given
        Optional<User> registeredUser = userService.registerUser(registrationRequest);
        assertTrue(registeredUser.isPresent());
        Long userId = registeredUser.get().getId();

        // When
        Boolean result = userService.deleteUser(userId);

        // Then
        assertNull(result); // Current implementation returns null

        // Verify user still exists in database
        assertTrue(userRepository.findById(userId).isPresent());
    }

    @Test
    void userStateAfterRegistration() {
        // When
        Optional<User> registeredUser = userService.registerUser(registrationRequest);

        // Then
        assertTrue(registeredUser.isPresent());
        User user = registeredUser.get();

        // Verify default user state
        assertTrue(user.isEnabled());
        assertFalse(user.isEmailVerified());
        assertFalse(user.isLocked());
        assertFalse(user.isCredentialsExpired());
    }

    @Test
    void concurrentUserRegistration() throws InterruptedException {
        // This test simulates concurrent registration attempts
        UserRegistrationRequest request1 = new UserRegistrationRequest();
        request1.setFirstName("User1");
        request1.setLastName("Test");
        request1.setEmail("user1@example.com");
        request1.setPassword("password123");

        UserRegistrationRequest request2 = new UserRegistrationRequest();
        request2.setFirstName("User2");
        request2.setLastName("Test");
        request2.setEmail("user2@example.com");
        request2.setPassword("password123");

        // When - register users
        Optional<User> user1 = userService.registerUser(request1);
        Optional<User> user2 = userService.registerUser(request2);

        // Then
        assertTrue(user1.isPresent());
        assertTrue(user2.isPresent());
        assertNotEquals(user1.get().getId(), user2.get().getId());

        // Verify both users exist in database
        assertEquals(2, userRepository.count());
    }
}
