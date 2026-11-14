package com.dd01xc.service.agent.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dd01xc.service.agent.model.AgentKeyDTOs.CreatedKey;
import com.dd01xc.service.agent.model.AgentKeyDTOs.KeyView;
import com.dd01xc.service.agent.security.AgentKeyAuthenticationFilter;
import com.dd01xc.service.agent.security.AgentPrincipal;
import com.dd01xc.service.agent.security.AgentSecurityConfig;
import com.dd01xc.service.agent.service.AgentKeyService;
import com.dd01xc.service.config.SecurityConfig;
import com.dd01xc.service.controller.ProfileController;
import com.dd01xc.service.repository.UserRepository;
import com.dd01xc.service.service.JwtService;

//both real chains: /api/agent/** takes only agent keys, everything else only user JWTs
@WebMvcTest({AgentController.class, AgentKeyController.class, ProfileController.class})
@Import({SecurityConfig.class, AgentSecurityConfig.class})
class AgentEndpointsSecurityTest {

    private static final String JWT = "user-jwt";
    private static final String PREFIX = "0123456789ab";
    //fake key built from parts; not a real secret
    private static final String AGENT_KEY = "amc_" + PREFIX + "_" + "f".repeat(64);
    private static final String KEY_HEADER = AgentKeyAuthenticationFilter.HEADER;
    private static final AgentPrincipal AGENT = new AgentPrincipal(1L, 7L, "laptop", PREFIX);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AgentKeyService agentKeyService;

    private void validJwtForJohn() {
        when(jwtService.extractUsername(JWT)).thenReturn("john");
        when(jwtService.validateToken(JWT, "john")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("john"))
            .thenReturn(User.withUsername("john").password("x").roles("USER").build());
        com.dd01xc.service.model.User john = mock(com.dd01xc.service.model.User.class);
        when(john.getId()).thenReturn(7L);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(john));
    }

    private void validAgentKey() {
        when(agentKeyService.authenticate(AGENT_KEY)).thenReturn(Optional.of(AGENT));
    }

    //agent side

    @Test
    void whoami_noKey_returns401() throws Exception {
        mockMvc.perform(get("/api/agent/whoami")).andExpect(status().isUnauthorized());
    }

    @Test
    void whoami_invalidKey_returns401() throws Exception {
        when(agentKeyService.authenticate(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/agent/whoami").header(KEY_HEADER, "amc_bogus"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void whoami_validKey_returnsKeyInfo_withoutSecret() throws Exception {
        validAgentKey();

        mockMvc.perform(get("/api/agent/whoami").header(KEY_HEADER, AGENT_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("laptop"))
            .andExpect(jsonPath("$.prefix").value("0123456789ab"))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("f".repeat(64)))));
    }

    @Test
    void whoami_userJwt_isNotAcceptedOnAgentApi() throws Exception {
        validJwtForJohn();

        mockMvc.perform(get("/api/agent/whoami").header("Authorization", "Bearer " + JWT))
            .andExpect(status().isUnauthorized());
    }

    //human side

    @Test
    void keyManagement_agentKey_isNotAcceptedOnUserApi() throws Exception {
        validAgentKey();

        mockMvc.perform(get("/api/agents/keys").header(KEY_HEADER, AGENT_KEY))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/profile/me").header(KEY_HEADER, AGENT_KEY))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(agentKeyService);
    }

    @Test
    void listKeys_jwt_returnsOwnKeys() throws Exception {
        validJwtForJohn();
        when(agentKeyService.list(7L)).thenReturn(List.of(
            new KeyView(1L, "laptop", "0123456789ab", Instant.parse("2026-01-01T12:00:00Z"), null, null)));

        mockMvc.perform(get("/api/agents/keys").header("Authorization", "Bearer " + JWT))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("laptop"))
            .andExpect(jsonPath("$[0].keyHash").doesNotExist());
    }

    @Test
    void createKey_jwt_returns201WithKey() throws Exception {
        validJwtForJohn();
        when(agentKeyService.create(7L, "laptop")).thenReturn(
            new CreatedKey(1L, "laptop", "0123456789ab", AGENT_KEY, Instant.parse("2026-01-01T12:00:00Z")));

        mockMvc.perform(post("/api/agents/keys").header("Authorization", "Bearer " + JWT)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"laptop\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.key").value(AGENT_KEY));
    }

    @Test
    void createKey_blankName_returns400() throws Exception {
        validJwtForJohn();

        mockMvc.perform(post("/api/agents/keys").header("Authorization", "Bearer " + JWT)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name").exists());
        verifyNoInteractions(agentKeyService);
    }

    @Test
    void createKey_overLimit_returns409() throws Exception {
        validJwtForJohn();
        when(agentKeyService.create(7L, "laptop"))
            .thenThrow(new com.dd01xc.service.service.exception.AgentKeyLimitException("limit"));

        mockMvc.perform(post("/api/agents/keys").header("Authorization", "Bearer " + JWT)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"laptop\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void revokeKey_jwt_returns204() throws Exception {
        validJwtForJohn();

        mockMvc.perform(delete("/api/agents/keys/1").header("Authorization", "Bearer " + JWT))
            .andExpect(status().isNoContent());
        verify(agentKeyService).revoke(7L, 1L);
    }

    @Test
    void revokeKey_notOwned_returns404() throws Exception {
        validJwtForJohn();
        doThrow(new com.dd01xc.service.service.exception.AgentKeyNotFoundException("Agent key not found"))
            .when(agentKeyService).revoke(7L, 2L);

        mockMvc.perform(delete("/api/agents/keys/2").header("Authorization", "Bearer " + JWT))
            .andExpect(status().isNotFound());
    }

    @Test
    void keyManagement_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/agents/keys")).andExpect(status().isUnauthorized());
    }
}
