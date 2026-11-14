package com.dd01xc.service.agent.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dd01xc.service.agent.model.AgentKey;

public interface AgentKeyRepository extends JpaRepository<AgentKey, Long> {

    Optional<AgentKey> findByKeyPrefix(String keyPrefix);

    List<AgentKey> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    //owner in the WHERE: another user's key looks exactly like a missing one
    Optional<AgentKey> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    long countByOwnerUserIdAndRevokedAtIsNull(Long ownerUserId);

    //single-column update instead of saving the whole entity on every agent request
    @Modifying
    @Query("UPDATE AgentKey k SET k.lastUsedAt = :usedAt WHERE k.id = :id")
    void touch(@Param("id") Long id, @Param("usedAt") Instant usedAt);
}
