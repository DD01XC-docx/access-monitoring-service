package com.dd01xc.service.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dd01xc.service.config.SecurityConfig;
import com.dd01xc.service.repository.UserRepository;
import com.dd01xc.service.service.AccessStatService;
import com.dd01xc.service.service.JwtService;

import io.jsonwebtoken.MalformedJwtException;

//security chain + real JwtAuthenticationFilter, services mocked
@WebMvcTest({ProfileController.class, AccessController.class})
@Import(SecurityConfig.class)
class ProtectedEndpointsTest {

    private static final String TOKEN = "test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AccessStatService accessStatService;

    private com.dd01xc.service.model.User john() {
        com.dd01xc.service.model.User user = new com.dd01xc.service.model.User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setRole("USER");
        user.setStatus("ACTIVE");
        return user;
    }

    private void tokenResolvesTo(UserDetails details) {
        when(jwtService.extractUsername(TOKEN)).thenReturn(details.getUsername());
        when(jwtService.validateToken(TOKEN, details.getUsername())).thenReturn(true);
        when(userDetailsService.loadUserByUsername(details.getUsername())).thenReturn(details);
    }

    @Test
    void profile_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessStats_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/access/stat/sla"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profile_validToken_returns200WithProfile() throws Exception {
        tokenResolvesTo(User.withUsername("john").password("x").roles("USER").build());
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(john()));

        mockMvc.perform(get("/api/profile/me").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void profile_malformedToken_returns401() throws Exception {
        when(jwtService.extractUsername(anyString())).thenThrow(new MalformedJwtException("bad"));

        mockMvc.perform(get("/api/profile/me").header("Authorization", "Bearer garbage"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profile_tokenOfDisabledUser_returns401() throws Exception {
        tokenResolvesTo(User.withUsername("john").password("x").roles("USER").disabled(true).build());

        mockMvc.perform(get("/api/profile/me").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ghost")
    void profile_userMissingInDb_returns404() throws Exception {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isNotFound());
    }
}
