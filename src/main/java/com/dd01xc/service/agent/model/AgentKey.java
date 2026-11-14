package com.dd01xc.service.agent.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//API key of an AI agent; the secret itself is never stored, only its SHA-256
@Entity
@Table(name = "agent_keys")
public class AgentKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;
    @Column(nullable = false, length = 64)
    private String name;
    @Column(name = "key_prefix", nullable = false, length = 16, unique = true)
    private String keyPrefix;
    @Column(name = "key_hash", nullable = false, length = 64)
    private String keyHash;
    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;
    @Column(name = "last_used_at", columnDefinition = "timestamptz")
    private Instant lastUsedAt;
    @Column(name = "revoked_at", columnDefinition = "timestamptz")
    private Instant revokedAt;

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
