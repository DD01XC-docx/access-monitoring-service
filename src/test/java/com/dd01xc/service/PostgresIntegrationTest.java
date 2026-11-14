package com.dd01xc.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dd01xc.service.agent.model.AgentKeyDTOs.CreatedKey;
import com.dd01xc.service.agent.repository.AgentKeyRepository;
import com.dd01xc.service.agent.service.AgentKeyService;
import com.dd01xc.service.model.AccessEvent;
import com.dd01xc.service.model.User;
import com.dd01xc.service.repository.AccessRepository;
import com.dd01xc.service.repository.UserRepository;

//real Postgres: Flyway migrations, Hibernate validate and native SQL (H2 can't run these)
@SpringBootTest(properties = {
    //base64 of "test-only-jwt-secret-not-for-production-use"
    "jwt.secret=dGVzdC1vbmx5LWp3dC1zZWNyZXQtbm90LWZvci1wcm9kdWN0aW9uLXVzZQ==",
})
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class PostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private Flyway flyway;
    @Autowired
    private AccessRepository accessRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AgentKeyService agentKeyService;
    @Autowired
    private AgentKeyRepository agentKeyRepository;

    //context start already proves ddl-auto=validate accepts the migrated schema
    @Test
    void migrationsApplied() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    void lastHourStats_bucketsAreIsoUtcAndCountOnlyRecentEvents() {
        Instant now = Instant.now();
        save("SUCCESS", now);
        save("FAILED", now);
        save("FAILED", now.minus(Duration.ofHours(3)));

        List<Object[]> rows = accessRepository.getStatsLastHourByMinute();

        assertThat(rows).hasSize(6);
        assertThat(rows).allSatisfy(row -> assertThat((String) row[0]).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:00Z"));
        assertThat(rows.stream().mapToLong(row -> ((Number) row[1]).longValue()).sum()).isEqualTo(1);
        assertThat(rows.stream().mapToLong(row -> ((Number) row[2]).longValue()).sum()).isEqualTo(1);
    }

    @Test
    void agentKey_createAuthenticateRevoke_onRealSchema() {
        User owner = saveUser("keyowner");
        CreatedKey created = agentKeyService.create(owner.getId(), "laptop");

        assertThat(agentKeyRepository.findByKeyPrefix(created.prefix()))
            .hasValueSatisfying(k -> assertThat(k.getKeyHash()).hasSize(64).doesNotContain(created.key().substring(17)));
        assertThat(agentKeyService.authenticate(created.key()))
            .hasValueSatisfying(p -> assertThat(p.ownerUserId()).isEqualTo(owner.getId()));

        agentKeyService.revoke(owner.getId(), created.id());
        assertThat(agentKeyService.authenticate(created.key())).isEmpty();
    }

    @Test
    void agentKeys_deletedWithOwner() {
        User owner = saveUser("shortlived");
        agentKeyService.create(owner.getId(), "ci");

        userRepository.delete(owner);
        userRepository.flush();

        assertThat(agentKeyRepository.findByOwnerUserIdOrderByCreatedAtDesc(owner.getId())).isEmpty();
    }

    private User saveUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("not-a-real-hash");
        user.setRole("USER");
        user.setStatus(User.STATUS_ACTIVE);
        user.setEnabled(true);
        user.setLastIp("127.0.0.1");
        user.setLastLogin(LocalDateTime.now());
        return userRepository.saveAndFlush(user);
    }

    private void save(String status, Instant createdAt) {
        AccessEvent event = new AccessEvent();
        event.setUsernameOrEmail("it@example.com");
        event.setStatus(status);
        event.setCreatedAt(createdAt);
        accessRepository.saveAndFlush(event);
    }
}
