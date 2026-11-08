package com.dd01xc.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.dd01xc.service.config.SecurityConfig;
import com.dd01xc.service.model.LoginRequest;
import com.dd01xc.service.model.LoginResponse;
import com.dd01xc.service.model.RegisterRequest;
import com.dd01xc.service.model.RegisterResponse;
import com.dd01xc.service.service.AuthService;
import com.dd01xc.service.service.JwtService;
import com.dd01xc.service.service.exception.DuplicateAccountException;
import com.dd01xc.service.service.exception.InvalidCredentialsException;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String LOGIN_JSON = """
            {"email":"john@example.com","password":"password123"}""";
    private static final String REGISTER_JSON = """
            {"username":"john","email":"john@example.com","password":"password123"}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    //required by JwtAuthenticationFilter
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void login_validBody_returns200WithToken() throws Exception {
        when(authService.login(any(LoginRequest.class), any()))
                .thenReturn(new LoginResponse("jwt-token", "john", "USER"));

        postJson("/api/auth/login", LOGIN_JSON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class), any()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        postJson("/api/auth/login", LOGIN_JSON)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void login_invalidEmail_returns400WithFieldError() throws Exception {
        postJson("/api/auth/login", """
                {"email":"john","password":"password123"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void login_passwordOver72Bytes_returns400() throws Exception {
        postJson("/api/auth/login", """
                {"email":"john@example.com","password":"%s"}""".formatted("a".repeat(73)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.passwordWithinBcryptLimit").exists());
    }

    @Test
    void login_xForwardedFor_passesFirstIpToService() throws Exception {
        when(authService.login(any(LoginRequest.class), any()))
                .thenReturn(new LoginResponse("jwt-token", "john", "USER"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_JSON)
                .header("X-Forwarded-For", "203.0.113.5, 10.0.0.1"))
                .andExpect(status().isOk());

        verify(authService).login(any(LoginRequest.class), eq("203.0.113.5"));
    }

    @Test
    void register_validBody_returns200() throws Exception {
        when(authService.register(any(RegisterRequest.class), any()))
                .thenReturn(new RegisterResponse("john", "USER"));

        postJson("/api/auth/register", REGISTER_JSON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void register_duplicate_returns409() throws Exception {
        when(authService.register(any(RegisterRequest.class), any()))
                .thenThrow(new DuplicateAccountException("Account already exists"));

        postJson("/api/auth/register", REGISTER_JSON)
                .andExpect(status().isConflict());
    }

    @Test
    void register_usernameWithSpace_returns400() throws Exception {
        postJson("/api/auth/register", """
                {"username":"john doe","email":"john@example.com","password":"password123"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists());

        verifyNoInteractions(authService);
    }
}
