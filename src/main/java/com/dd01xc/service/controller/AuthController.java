package com.dd01xc.service.controller;

import com.dd01xc.service.model.AccessEvent;
import com.dd01xc.service.model.User;
import com.dd01xc.service.repository.AccessRepository;
import com.dd01xc.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final String ACCESS_STATUS_SUCCESS = "SUCCESS";
    private static final String ACCESS_STATUS_FAILED = "FAILED";
    private static final String USER_ROLE = "USER";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private static final String EMAIL_EXISTS_MESSAGE = "Email already exists";
    private static final String USERNAME_EXISTS_MESSAGE = "Username already exists";
    private static final String REGISTER_SUCCESS_MESSAGE = "User registered successfully";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccessRepository accessRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = findUserByEmailOrUsername(loginRequest.getEmail());
        AccessEvent accessEvent = createAccessEvent(loginRequest.getEmail());

        if (isValidCredentials(userOptional, loginRequest.getPassword())) {
            accessEvent.setStatus(ACCESS_STATUS_SUCCESS);
            accessRepository.save(accessEvent);
            User user = userOptional.get();
            Map<String, String> response = new HashMap<>();
            response.put("token", "jwt-token-placeholder");
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            return ResponseEntity.ok(response);
        }

        accessEvent.setStatus(ACCESS_STATUS_FAILED);
        accessRepository.save(accessEvent);
        return ResponseEntity.status(401).body(INVALID_CREDENTIALS_MESSAGE);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(EMAIL_EXISTS_MESSAGE);
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(USERNAME_EXISTS_MESSAGE);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(USER_ROLE);
        user.setEnabled(true);
        user.setStatus(USER_STATUS_ACTIVE);
        userRepository.save(user);
        return ResponseEntity.ok(REGISTER_SUCCESS_MESSAGE);
    }
    //userfinder
    private Optional<User> findUserByEmailOrUsername(String emailOrUsername) {
        return userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername));
    }
    //access
    private AccessEvent createAccessEvent(String usernameOrEmail) {
        AccessEvent accessEvent = new AccessEvent();
        accessEvent.setUsernameOrEmail(usernameOrEmail);
        return accessEvent;
    }
    //valid
    private boolean isValidCredentials(Optional<User> userOptional, String rawPassword) {
        return userOptional.isPresent() && passwordEncoder.matches(rawPassword, userOptional.get().getPassword());
    }
    //login
    public static class LoginRequest {
        private String email;
        private String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    //register
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    //forgot
    public static class ForgotPasswordRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}