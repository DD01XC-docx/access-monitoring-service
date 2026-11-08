package com.dd01xc.service.model;

import java.nio.charset.StandardCharsets;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min=3, max=20, message = "Username must be been between 3 and 20")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Username may contain only letters, digits, '_', '.' and '-'")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    @Size(max=254, message = "Email must be at most 254 characters")
    private String email;


    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    //bcrypt ignores bytes past 72
    @AssertTrue(message = "Password must be at most 72 bytes")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    //get-set
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
