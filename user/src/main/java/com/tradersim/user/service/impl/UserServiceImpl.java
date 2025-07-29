package com.tradersim.user.service.impl;

import com.tradersim.user.dto.UserRegistrationRequest;
import com.tradersim.user.dto.UserResponse;
import com.tradersim.user.exception.UserAlreadyExistsException;
import com.tradersim.user.model.Role;
import com.tradersim.user.model.RoleName;
import com.tradersim.user.model.User;
import com.tradersim.user.repository.RoleRepository;
import com.tradersim.user.repository.UserRepository;
import com.tradersim.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> registerUser(UserRegistrationRequest request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        // Find the default USER role
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setEmailVerified(false); // Email verification can be implemented later
        user.setLocked(false);
        user.setCredentialsExpired(false);

        // Create a new HashSet and add the role
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        return Optional.of(userRepository.save(user));
    }

    @Override
    public Boolean deleteUser(Long id) {
        return null;
    }

    @Override
    public List<UserResponse> listUsers(int page, int size) {
        return List.of();
    }
}