package com.beartrail.user.controller;

import com.beartrail.user.dto.AuthResponse;
import com.beartrail.user.dto.LoginRequest;
import com.beartrail.user.dto.UserRegistrationRequest;
import com.beartrail.user.exception.UserAlreadyExistsException;
import com.beartrail.user.model.User;
import com.beartrail.user.security.JwtUtil;
import com.beartrail.user.service.AuthService;
import com.beartrail.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController (AuthService authService, UserService userService, JwtUtil jwtUtil)  {
        this.authService = authService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        try {
            AuthResponse response = authService.authenticate(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception exception) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .success(false)
                    .message(exception.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        try {
            User user = userService.registerUser(request).orElseThrow();
            AuthResponse response = AuthResponse.builder()
                    .success(true)
                    .message("User registered successfully")
                    .userId(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .build();
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .success(false)
                    .message("Registration failed: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            AuthResponse errorResponse = AuthResponse.builder()
                    .success(false)
                    .message("Registration failed: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
