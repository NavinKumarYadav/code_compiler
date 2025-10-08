package com.compiler.controller;

import com.compiler.dto.AuthRequest;
import com.compiler.dto.AuthResponse;
import com.compiler.dto.LoginRequest;
import com.compiler.entity.User;
import com.compiler.security.JwtUtil;
import com.compiler.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager){
        this.userService=userService;
        this.jwtUtil=jwtUtil;
        this.authenticationManager=authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody LoginRequest loginRequest){
       logger.info("Registration attempt for email: {}", loginRequest.getEmail());

       try {
           if(userService.existsByEmail(loginRequest.getEmail())){
               return ResponseEntity.badRequest().body("Email already exists");
           }

           User user = userService.register(loginRequest.getEmail(), loginRequest.getPassword());

           String token = jwtUtil.generateToken(user.getEmail());

           AuthResponse authResponse = new AuthResponse(token, user.getEmail(), 3600000L);

           logger.info("Registration successful for email: {}", loginRequest.getEmail());

           return ResponseEntity.ok(authResponse);
       } catch (Exception e) {
           logger.error("Registration failed for email: {}", loginRequest.getEmail(), e);

           return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
       }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            String token = jwtUtil.generateToken(loginRequest.getEmail());

            User user = userService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AuthResponse authResponse = new AuthResponse(token, user.getEmail(), 3600000L);

            logger.info("Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth controller is working! ✅");
    }
}
