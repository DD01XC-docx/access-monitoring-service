package com.dd01xc.service.agent.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dd01xc.service.agent.service.AgentKeyService;

//machine API: /api/agent/** accepts only X-AMC-Agent-Key, never a user JWT.
//Ordered before the default chain in SecurityConfig, which handles everything else.
@Configuration
public class AgentSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain agentFilterChain(HttpSecurity http, AgentKeyService agentKeyService) throws Exception {
        http
            .securityMatcher("/api/agent/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth.anyRequest().hasAuthority(AgentKeyAuthenticationFilter.ROLE))
            .addFilterBefore(new AgentKeyAuthenticationFilter(agentKeyService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
