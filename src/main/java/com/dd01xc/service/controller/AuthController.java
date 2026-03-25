package com.dd01xc.service.controller;

import com.dd01xc.service.model.LoginRequest;
import com.dd01xc.service.model.LoginResponse;
import com.dd01xc.service.model.RegisterRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dd01xc.service.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

import com.dd01xc.service.model.RegisterResponse;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) { 
        String clientIp = getClientIp(httpRequest);
        LoginResponse response = authService.login(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        RegisterResponse response = authService.register(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkAuth() {
        String response  = authService.checkAuth();
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
    String remoteAddr = request.getHeader("X-Forwarded-For");
    if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
        remoteAddr = request.getRemoteAddr();
    }
    return remoteAddr.contains(",") ? remoteAddr.split(",")[0].trim() : remoteAddr;
}
}
