package com.dd01xc.service.agent.model;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//request/response shapes for agent key management; hashes never leave the service
public final class AgentKeyDTOs {

    private AgentKeyDTOs() {}

    public record CreateKeyRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 64, message = "Name must be at most 64 characters")
        String name
    ) {}

    //only response that carries the full key, returned once on creation
    public record CreatedKey(Long id, String name, String prefix, String key, Instant createdAt) {}

    public record KeyView(Long id, String name, String prefix, Instant createdAt, Instant lastUsedAt, Instant revokedAt) {
        public static KeyView from(AgentKey k) {
            return new KeyView(k.getId(), k.getName(), k.getKeyPrefix(), k.getCreatedAt(), k.getLastUsedAt(), k.getRevokedAt());
        }
    }

    //what the agent sees about itself
    public record WhoAmI(Long keyId, String name, String prefix) {}
}
