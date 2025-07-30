package com.beartrail.user.service;

import com.beartrail.user.dto.UserRegistrationRequest;
import com.beartrail.user.dto.UserResponse;
import com.beartrail.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findUserById(Long id);
    Optional<User> findUserByEmail(String email);
    Optional<User> registerUser(UserRegistrationRequest userRegistrationRequest);
    Boolean deleteUser(Long id);
    List<UserResponse> listUsers(int page, int size);
} 