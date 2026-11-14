package com.dd01xc.service.agent.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dd01xc.service.agent.service.AgentKeyService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//not a @Component on purpose: Boot would also register it as a global servlet filter
public class AgentKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-AMC-Agent-Key";
    public static final String ROLE = "ROLE_AGENT";

    private final AgentKeyService agentKeyService;

    public AgentKeyAuthenticationFilter(AgentKeyService agentKeyService) {
        this.agentKeyService = agentKeyService;
    }

    //missing or bad key leaves the context anonymous, the agent chain answers 401
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String rawKey = request.getHeader(HEADER);
        if (rawKey != null) {
            agentKeyService.authenticate(rawKey.strip()).ifPresent(principal ->
                SecurityContextHolder.getContext().setAuthentication(new AgentAuthentication(principal)));
        }
        chain.doFilter(request, response);
    }

    static final class AgentAuthentication extends AbstractAuthenticationToken {
        private final AgentPrincipal principal;

        AgentAuthentication(AgentPrincipal principal) {
            super(List.of(new SimpleGrantedAuthority(ROLE)));
            this.principal = principal;
            setAuthenticated(true);
        }

        //the key is never kept in memory after the check
        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public AgentPrincipal getPrincipal() {
            return principal;
        }

        @Override
        public String getName() {
            return "agent:" + principal.prefix();
        }
    }
}
