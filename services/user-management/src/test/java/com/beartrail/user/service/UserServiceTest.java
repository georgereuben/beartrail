package com.beartrail.user.service;

import com.beartrail.user.dto.UserRegistrationRequest;
import com.beartrail.user.dto.UserResponse;
import com.beartrail.user.exception.UserAlreadyExistsException;
import com.beartrail.user.model.Role;
import com.beartrail.user.model.RoleName;
import com.beartrail.user.model.User;
import com.beartrail.user.repository.RoleRepository;
import com.beartrail.user.repository.UserRepository;
import com.beartrail.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setEmailVerified(false);

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setEmail("john.doe@example.com");
        registrationRequest.setPassword("password123");
    }

    @Test
    void findUserById_UserExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        assertEquals(testUser.getFirstName(), result.get().getFirstName());
        assertEquals(testUser.getLastName(), result.get().getLastName());
    }

    @Test
    void findUserById_UserNotExists() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findUserById(userId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findUserByEmail_UserExists() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findUserByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        assertEquals(testUser.getFirstName(), result.get().getFirstName());
    }

    @Test
    void findUserByEmail_UserNotExists() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findUserByEmail(email);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void registerUser_Success() {
        // Given
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(RoleName.ROLE_USER);

        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Optional<User> result = userService.registerUser(registrationRequest);

        // Then
        assertTrue(result.isPresent());
        User savedUser = result.get();
        assertEquals(registrationRequest.getFirstName(), savedUser.getFirstName());
        assertEquals(registrationRequest.getLastName(), savedUser.getLastName());
        assertEquals(registrationRequest.getEmail(), savedUser.getEmail());
        
        verify(roleRepository).findByName(RoleName.ROLE_USER);
        verify(passwordEncoder).encode(registrationRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Given
        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(registrationRequest)
        );

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerUser_WithDifferentUserData() {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@example.com");
        request.setPassword("differentPassword");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName(RoleName.ROLE_USER);

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setFirstName("Jane");
        savedUser.setLastName("Smith");
        savedUser.setEmail("jane.smith@example.com");
        savedUser.setPassword("encodedDifferentPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedDifferentPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        Optional<User> result = userService.registerUser(request);

        // Then
        assertTrue(result.isPresent());
        User user = result.get();
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("jane.smith@example.com", user.getEmail());

        verify(roleRepository).findByName(RoleName.ROLE_USER);
    }

    @Test
    void deleteUser_NotImplemented() {
        // Given
        Long userId = 1L;

        // When
        Boolean result = userService.deleteUser(userId);

        // Then
        assertNull(result); // Current implementation returns null
    }

    @Test
    void listUsers_NotImplemented() {
        // Given
        int page = 0;
        int size = 10;

        // When
        List<UserResponse> result = userService.listUsers(page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Current implementation returns empty list
    }
}
