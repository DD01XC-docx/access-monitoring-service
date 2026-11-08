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
import com.dd01xc.service.service.exception.DuplicateAccountException;
import com.dd01xc.service.service.exception.InvalidCredentialsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {
    //const
    private static final String ACCESS_STATUS_SUCCESS = "SUCCESS";
    private static final String ACCESS_STATUS_FAILED = "FAILED";
    private static final String USER_ROLE = "USER";
    private static final String USER_STATUS_ACTIVE = User.STATUS_ACTIVE;
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private static final String DUPLICATE_ACCOUNT_MESSAGE = "Account already exists";
    //bcrypt cost 10, equalizes login timing for unknown users
    private static final String DUMMY_PASSWORD_HASH = "$2a$10$20stZrpXygUjw0w3jLHhD.yGIdDATOTdpZVykwnflleruFGoBFkS2";
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
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
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        
        AccessEvent accessEvent = createAccessEvent(loginRequest.getEmail(), clientIp);

        if (!isValidCredentials(userOptional, loginRequest.getPassword())) {
            saveAccessEvent(accessEvent, ACCESS_STATUS_FAILED, startTime);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        User user = userOptional.get();
        //same 401 as bad password, reason only in server log
        if (!user.isActive()) {
            log.warn("Login rejected for inactive account: {}", user.getUsername());
            saveAccessEvent(accessEvent, ACCESS_STATUS_FAILED, startTime);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        user.setLastLogin(LocalDateTime.now());
        user.setLastIp(clientIp);
        userRepository.save(user);

        saveAccessEvent(accessEvent, ACCESS_STATUS_SUCCESS, startTime);
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    //reg

    @Transactional(noRollbackFor = DuplicateAccountException.class)
    public RegisterResponse register(RegisterRequest registerRequest, String clientIp) {
        long startTime = System.nanoTime();
        AccessEvent accessEvent = createAccessEvent(registerRequest.getEmail(), clientIp);

        if (userRepository.existsByEmail(registerRequest.getEmail())
                || userRepository.existsByUsername(registerRequest.getUsername())) {
            saveAccessEvent(accessEvent, ACCESS_STATUS_FAILED, startTime);
            throw new DuplicateAccountException(DUPLICATE_ACCOUNT_MESSAGE);
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
        return new RegisterResponse(user.getUsername(), user.getRole());
    }

    //extra-help-func

    public String checkAuth() {
        return "Authenticated successfully";
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
        //always run bcrypt so unknown users take as long as known ones
        String hash = userOptional.map(User::getPassword).orElse(DUMMY_PASSWORD_HASH);
        boolean matches = passwordEncoder.matches(rawPassword, hash);
        return userOptional.isPresent() && matches;
    }

    public static class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
