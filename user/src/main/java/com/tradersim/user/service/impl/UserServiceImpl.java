package com.tradersim.user.service.impl;

import com.tradersim.user.dto.UserRegistrationRequest;
import com.tradersim.user.dto.UserResponse;
import com.tradersim.user.model.User;
import com.tradersim.user.repository.UserRepository;
import com.tradersim.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private UserRegistrationRequest request;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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