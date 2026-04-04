package com.dd01xc.service.controller;

import com.dd01xc.service.repository.AccessRepository;
import com.dd01xc.service.repository.UserRepository;
import com.dd01xc.service.model.ProfileResponseDTO;

import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    
    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
            .map(user -> {
                ProfileResponseDTO profile = new ProfileResponseDTO(
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus(),
                    user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "N/A",
                    user.getLastLogin() != null ? user.getLastLogin().format(formatter) : "Never",
                    user.getLastIp() != null ? user.getLastIp() : "Unknown"
                );
                return ResponseEntity.ok(profile);
            })
            .orElse(ResponseEntity.status(404).build());
    }
}