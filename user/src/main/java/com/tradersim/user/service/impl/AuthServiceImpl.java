package com.tradersim.user.service.impl;

import com.tradersim.user.dto.AuthResponse;
import com.tradersim.user.dto.LoginRequest;
import com.tradersim.user.exception.UserNotFoundException;
import com.tradersim.user.model.Role;
import com.tradersim.user.model.RoleName;
import com.tradersim.user.model.User;
import com.tradersim.user.repository.UserRepository;
import com.tradersim.user.security.JwtUtil;
import com.tradersim.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           UserDetailsService userDetailsService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse authenticate(LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authToken);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            Set<RoleName> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            long expiresIn = jwtUtil.getJwtExpiration();
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(expiresIn)
                    .expiresAt(expiresAt)
                    .userId(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .roles(roleNames)
                    .emailVerified(user.isEmailVerified())
                    .enabled(user.isEnabled())
                    .success(true)
                    .message("Authentication successful")
                    .build();

        } catch (AuthenticationException exception) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Authentication failed: " + exception.getMessage())
                    .build();
        } catch (Exception exception) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Authentication error: " + exception.getMessage())
                    .build();
        }
    }

    @Override
    public String refreshToken(String oldToken) {
        try {
            String username = jwtUtil.extractUsername(oldToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if(jwtUtil.validateToken(oldToken, userDetails)) {
                return jwtUtil.generateToken(userDetails);
            } else {
                throw new BadCredentialsException("Invalid refresh token");
            }
        } catch (Exception exception) {
            throw new BadCredentialsException("Failed to refresh token: " + exception.getMessage());
        }
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Optionally: void initiatePasswordReset(String email);
    
}
