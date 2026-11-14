package com.dd01xc.service.agent.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dd01xc.service.agent.model.AgentKeyDTOs.WhoAmI;
import com.dd01xc.service.agent.security.AgentPrincipal;

//machine side (X-AMC-Agent-Key): endpoints called by agent hooks
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    //lets a hook verify its key and connectivity
    @GetMapping("/whoami")
    public WhoAmI whoami(@AuthenticationPrincipal AgentPrincipal agent) {
        return new WhoAmI(agent.keyId(), agent.name(), agent.prefix());
    }
}
