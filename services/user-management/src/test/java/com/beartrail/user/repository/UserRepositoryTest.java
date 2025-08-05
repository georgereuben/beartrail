package com.beartrail.user.repository;

import com.beartrail.user.TestConstants;
import com.beartrail.user.model.Role;
import com.beartrail.user.model.RoleName;
import com.beartrail.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Create and persist role
        userRole = new Role();
        userRole.setName(RoleName.ROLE_USER);
        entityManager.persistAndFlush(userRole);

        // Create test user
        testUser = new User();
        testUser.setFirstName(TestConstants.TEST_FIRST_NAME);
        testUser.setLastName(TestConstants.TEST_LAST_NAME);
        testUser.setEmail(TestConstants.TEST_EMAIL);
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setEmailVerified(true);
        testUser.setRoles(new HashSet<>(Set.of(userRole)));
    }

    @Test
    void save_NewUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals(TestConstants.TEST_FIRST_NAME, savedUser.getFirstName());
        assertEquals(TestConstants.TEST_LAST_NAME, savedUser.getLastName());
        assertEquals(TestConstants.TEST_EMAIL, savedUser.getEmail());
        assertTrue(savedUser.isEnabled());
        assertTrue(savedUser.isEmailVerified());
        assertEquals(1, savedUser.getRoles().size());
    }

    @Test
    void save_UpdateExistingUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        savedUser.setFirstName("Jane");
        savedUser.setLastName("Smith");
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("Jane", updatedUser.getFirstName());
        assertEquals("Smith", updatedUser.getLastName());
    }

    @Test
    void findById_UserExists() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("john.doe@example.com", foundUser.get().getEmail());
    }

    @Test
    void findById_UserNotExists() {
        // When
        Optional<User> foundUser = userRepository.findById(999L);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByEmail_UserExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("John", foundUser.get().getFirstName());
        assertEquals("Doe", foundUser.get().getLastName());
    }

    @Test
    void findByEmail_UserNotExists() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByEmail_CaseInsensitive() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmailIgnoreCase("JOHN.DOE@EXAMPLE.COM");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("john.doe@example.com", foundUser.get().getEmail());
    }

    @Test
    void existsByEmail_UserExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByEmail_UserNotExists() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    void findAll_MultipleUsers() {
        // Given
        User secondUser = new User();
        secondUser.setFirstName("Jane");
        secondUser.setLastName("Smith");
        secondUser.setEmail("jane.smith@example.com");
        secondUser.setPassword("encodedPassword");
        secondUser.setEnabled(true);
        secondUser.setEmailVerified(true);
        secondUser.setRoles(new HashSet<>(Set.of(userRole)));

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(secondUser);

        // When
        List<User> users = userRepository.findAll();

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("john.doe@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("jane.smith@example.com")));
    }

    @Test
    void delete_UserExists() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Long userId = savedUser.getId();

        // When
        userRepository.delete(savedUser);
        entityManager.flush();

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void userRolesRelationship() {
        // Given
        Role adminRole = new Role();
        adminRole.setName(RoleName.ROLE_ADMIN);
        entityManager.persistAndFlush(adminRole);

        testUser.setRoles(new HashSet<>(Set.of(userRole, adminRole)));
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertThat(foundUser.get().getRoles()).hasSize(2);
        assertTrue(foundUser.get().getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_USER));
        assertTrue(foundUser.get().getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN));
    }
}
