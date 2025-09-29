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
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest){
        User u = userService.register(authRequest.getUserName(), authRequest.getUserName()+"@mail.com",authRequest.getPassword());
        String token = jwtUtil.generateToken(u.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authRequest.getUserName(),authRequest.getPassword());
        authenticationManager.authenticate(authInputToken);
        String token = jwtUtil.generateToken(authRequest.getUserName());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
