package com.dd01xc.service.agent.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.dd01xc.service.agent.model.AgentKeyDTOs.CreateKeyRequest;
import com.dd01xc.service.agent.model.AgentKeyDTOs.CreatedKey;
import com.dd01xc.service.agent.model.AgentKeyDTOs.KeyView;
import com.dd01xc.service.agent.service.AgentKeyService;
import com.dd01xc.service.repository.UserRepository;

import jakarta.validation.Valid;

//human side (JWT): manage own agent keys
@RestController
@RequestMapping("/api/agents/keys")
public class AgentKeyController {

    private final AgentKeyService agentKeyService;
    private final UserRepository userRepository;

    public AgentKeyController(AgentKeyService agentKeyService, UserRepository userRepository) {
        this.agentKeyService = agentKeyService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedKey create(@Valid @RequestBody CreateKeyRequest request, Authentication auth) {
        return agentKeyService.create(currentUserId(auth), request.name());
    }

    @GetMapping
    public List<KeyView> list(Authentication auth) {
        return agentKeyService.list(currentUserId(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable Long id, Authentication auth) {
        agentKeyService.revoke(currentUserId(auth), id);
    }

    //JWT filter already checked the user exists; a race with deletion fails closed
    private Long currentUserId(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
            .getId();
    }
}
