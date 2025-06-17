package com.tradersim.user.service;

import com.tradersim.user.dto.UserRegistrationRequest;
import com.tradersim.user.dto.UserResponse;
import com.tradersim.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findUserById(Long id);
    Optional<User> findUserByEmail(String email);
    Optional<User> registerUser(UserRegistrationRequest userRegistrationRequest);
    Boolean deleteUser(Long id);
    List<UserResponse> listUsers(int page, int size);
} 