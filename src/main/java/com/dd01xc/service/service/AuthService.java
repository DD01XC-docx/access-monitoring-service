package com.dd01xc.service.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dd01xc.service.model.AccessEvent;
import com.dd01xc.service.model.LoginRequest;
import com.dd01xc.service.model.LoginResponse;
import com.dd01xc.service.model.RegisterRequest;
import com.dd01xc.service.model.RegisterResponse;
import com.dd01xc.service.model.User;
import com.dd01xc.service.repository.AccessRepository;
import com.dd01xc.service.repository.UserRepository;
import com.dd01xc.service.service.exception.InvalidCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {
    //const
    private static final String ACCESS_STATUS_SUCCESS = "SUCCESS";
    private static final String ACCESS_STATUS_FAILED = "FAILED";
    private static final String USER_ROLE = "USER";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AccessRepository accessRepository;
    private final JwtService jwtService;

    public AuthService (PasswordEncoder passwordEncoder, UserRepository userRepository, AccessRepository accessRepository, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.accessRepository = accessRepository;
        this.jwtService = jwtService;
    }
    
    //log
    
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginResponse login(LoginRequest loginRequest, String clientIp) {

        long startTime = System.nanoTime();
        Optional<User> userOptional = findUserByEmailOrUsername(loginRequest.getEmail());
        
        AccessEvent accessEvent = createAccessEvent(loginRequest.getEmail(), clientIp);

        if (!isValidCredentials(userOptional, loginRequest.getPassword())) {
            saveAccessEvent(accessEvent, ACCESS_STATUS_FAILED, startTime);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        User user = userOptional.get();
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        user.setLastLogin(LocalDateTime.now());
        user.setLastIp(clientIp);
        userRepository.save(user);

        saveAccessEvent(accessEvent, ACCESS_STATUS_SUCCESS, startTime);
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    //reg

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public RegisterResponse register(RegisterRequest registerRequest, String clientIp) {
        long startTime = System.nanoTime();
        AccessEvent accessEvent = createAccessEvent(registerRequest.getEmail(), clientIp);

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            accessEvent.setStatus(ACCESS_STATUS_FAILED);
            accessEvent.setDurationMs((System.nanoTime() - startTime) / 1_000_000);
            accessRepository.save(accessEvent);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            accessEvent.setStatus(ACCESS_STATUS_FAILED);
            accessEvent.setDurationMs((System.nanoTime() - startTime) / 1_000_000);
            accessRepository.save(accessEvent);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(USER_ROLE);
        user.setEnabled(true);
        user.setStatus(USER_STATUS_ACTIVE);
        user.setLastLogin(LocalDateTime.now());
        user.setLastIp(clientIp);
        userRepository.save(user);
        
        saveAccessEvent(accessEvent, ACCESS_STATUS_SUCCESS, startTime);
        return new RegisterResponse(user.getEmail(), user.getRole());
    }

    //extra-help-func

    public String checkAuth() {
        return "Authenticated successfully";
    }

     private Optional<User> findUserByEmailOrUsername(String emailOrUsername) {
        return userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername));
    }

    private AccessEvent createAccessEvent(String usernameOrEmail, String clientIp) {
        AccessEvent accessEvent = new AccessEvent();
        accessEvent.setUsernameOrEmail(usernameOrEmail);
        accessEvent.setIpAddress(clientIp);
        return accessEvent;
    }

   private void saveAccessEvent(AccessEvent accessEvent, String status, long startTime) {
        accessEvent.setStatus(status);
        accessEvent.setDurationMs((System.nanoTime() - startTime) / 1_000_000);
        accessRepository.save(accessEvent);
    }

    private boolean isValidCredentials(Optional<User> userOptional, String rawPassword) {
        return userOptional.isPresent() && passwordEncoder.matches(rawPassword, userOptional.get().getPassword());
    }

    public static class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
