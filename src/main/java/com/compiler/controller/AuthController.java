package com.compiler.controller;

import com.compiler.dto.AuthRequest;
import com.compiler.dto.AuthResponse;
import com.compiler.entity.User;
import com.compiler.security.JwtUtil;
import com.compiler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {



    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager){
        this.userService=userService;
        this.jwtUtil=jwtUtil;
        this.authenticationManager=authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest){
        if(userService.existsByUsername(authRequest.getUserName())){
            return ResponseEntity.badRequest().body("Username already exists");
        }
        User u = userService.register(authRequest.getUserName(),
                authRequest.getEmail(),authRequest.getPassword());

        String token = jwtUtil.generateToken(u.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUserName(),
                            authRequest.getPassword())
            );
            String token = jwtUtil.generateToken(authRequest.getUserName());
            return ResponseEntity.ok(new AuthResponse(token));
        }catch (Exception e){
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
