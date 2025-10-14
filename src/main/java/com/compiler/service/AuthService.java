package com.compiler.service;

import com.compiler.dto.RegisterRequest;
import com.compiler.entity.User;
import com.compiler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public User register(RegisterRequest request) {
        try {
            log.info("Attempting registration for user: {}", request.getUsername());

            if (userService.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already exists");
            }

            if (userService.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already registered");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setIsActive(true);

            User savedUser = userRepository.save(user);
            log.info("User registered successfully: {}", savedUser.getUsername());

            return savedUser;

        } catch (Exception e) {
            log.error("Registration failed for user {}: {}", request.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
}