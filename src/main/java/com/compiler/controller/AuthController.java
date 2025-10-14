package com.compiler.controller;

import com.compiler.dto.AuthResponse;
import com.compiler.dto.LoginRequest;
import com.compiler.dto.RegisterRequest;
import com.compiler.entity.User;
import com.compiler.security.JwtUtil;
import com.compiler.service.UserService;
import com.compiler.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> getLoginInfo() {
        log.info("GET request to /auth/login");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Use POST method to login");
        response.put("example", Map.of(
                "method", "POST",
                "content-type", "application/json",
                "body", Map.of(
                        "username", "your_username",
                        "password", "your_password"
                )
        ));
        response.put("endpoint", "/auth/login");
        response.put("supported_methods", new String[]{"POST"});

        return ResponseEntity.ok(response);
    }

    @GetMapping("/register")
    public ResponseEntity<Map<String, Object>> getRegisterInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Use POST method to register");
        response.put("example", Map.of(
                "method", "POST",
                "content-type", "application/json",
                "body", Map.of(
                        "username", "new_user",
                        "email", "user@example.com",
                        "password", "password123"
                )
        ));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for user: {}", registerRequest.getUsername());

        try {
            if(userService.existsByEmail(registerRequest.getEmail())){
                return ResponseEntity.badRequest().body("Email already exists");
            }

            if(userService.existsByUsername(registerRequest.getUsername())){
                return ResponseEntity.badRequest().body("Username already exists");
            }

            User user = userService.register(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword()
            );

            String token = jwtUtil.generateToken(user.getUsername());

            AuthResponse authResponse = new AuthResponse(token, user.getEmail(), 3600000L);

            log.info("Registration successful for user: {}", registerRequest.getUsername());

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Registration failed for user: {}", registerRequest.getUsername(), e);
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = userService.findByUsername(loginRequest.getUsername());

            String token = jwtUtil.generateToken(user.getUsername());

            AuthResponse authResponse = new AuthResponse(token, user.getEmail(), 3600000L);

            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth controller is working! ✅");
    }
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);

                if (username != null) {
                    UserDetails userDetails = userService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(token, userDetails)) {
                        User user = userService.findByUsername(username);
                        return ResponseEntity.ok(Map.of(
                                "valid", true,
                                "username", user.getUsername(),
                                "email", user.getEmail()
                        ));
                    }
                }
            }
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Token verification failed"));
        }
    }
}