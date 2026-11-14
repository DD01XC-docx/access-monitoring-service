package com.dd01xc.service.agent.security;

//authenticated agent: which key, and which human owns it
public record AgentPrincipal(Long keyId, Long ownerUserId, String name, String prefix) {}
