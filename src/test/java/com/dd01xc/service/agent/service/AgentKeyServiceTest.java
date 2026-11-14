package com.dd01xc.service.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dd01xc.service.agent.model.AgentKey;
import com.dd01xc.service.agent.model.AgentKeyDTOs.CreatedKey;
import com.dd01xc.service.agent.repository.AgentKeyRepository;
import com.dd01xc.service.agent.security.AgentPrincipal;
import com.dd01xc.service.model.User;
import com.dd01xc.service.repository.UserRepository;
import com.dd01xc.service.service.exception.AgentKeyLimitException;
import com.dd01xc.service.service.exception.AgentKeyNotFoundException;

@ExtendWith(MockitoExtension.class)
class AgentKeyServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final Long OWNER = 7L;
    private static final String PREFIX = "0123456789ab";
    private static final String SECRET = "f".repeat(64);
    private static final String RAW_KEY = "amc_" + PREFIX + "_" + SECRET;

    @Mock
    private AgentKeyRepository keyRepository;
    @Mock
    private UserRepository userRepository;

    private AgentKeyService service;

    @BeforeEach
    void setUp() {
        service = new AgentKeyService(keyRepository, userRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private AgentKey storedKey() {
        AgentKey key = new AgentKey();
        key.setId(1L);
        key.setOwnerUserId(OWNER);
        key.setName("laptop");
        key.setKeyPrefix(PREFIX);
        key.setKeyHash(AgentKeyService.sha256Hex(SECRET));
        key.setCreatedAt(NOW.minus(Duration.ofDays(1)));
        return key;
    }

    private void ownerActive(boolean active) {
        User owner = new User();
        owner.setEnabled(active);
        owner.setStatus("ACTIVE");
        when(userRepository.findById(OWNER)).thenReturn(Optional.of(owner));
    }

    @Test
    void create_returnsKeyOnce_andStoresOnlyItsHash() {
        CreatedKey created = service.create(OWNER, "  laptop  ");

        assertThat(created.key()).matches("amc_[0-9a-f]{12}_[0-9a-f]{64}");
        assertThat(created.prefix()).isEqualTo(created.key().substring(4, 16));
        assertThat(created.name()).isEqualTo("laptop");

        ArgumentCaptor<AgentKey> saved = ArgumentCaptor.forClass(AgentKey.class);
        verify(keyRepository).save(saved.capture());
        String secret = created.key().substring(17);
        assertThat(saved.getValue().getKeyHash())
            .isEqualTo(AgentKeyService.sha256Hex(secret))
            .doesNotContain(secret);
        assertThat(saved.getValue().getOwnerUserId()).isEqualTo(OWNER);
        assertThat(saved.getValue().getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void create_generatesDifferentKeys() {
        assertThat(service.create(OWNER, "a").key()).isNotEqualTo(service.create(OWNER, "b").key());
    }

    @Test
    void create_atLimit_throws() {
        when(keyRepository.countByOwnerUserIdAndRevokedAtIsNull(OWNER)).thenReturn((long) AgentKeyService.MAX_ACTIVE_KEYS);

        assertThatThrownBy(() -> service.create(OWNER, "one too many")).isInstanceOf(AgentKeyLimitException.class);
        verify(keyRepository, never()).save(any());
    }

    @Test
    void authenticate_validKey_returnsPrincipal_andTouchesLastUsed() {
        when(keyRepository.findByKeyPrefix(PREFIX)).thenReturn(Optional.of(storedKey()));
        ownerActive(true);

        Optional<AgentPrincipal> principal = service.authenticate(RAW_KEY);

        assertThat(principal).contains(new AgentPrincipal(1L, OWNER, "laptop", PREFIX));
        verify(keyRepository).touch(1L, NOW);
    }

    @Test
    void authenticate_recentlyUsed_doesNotWriteAgain() {
        AgentKey key = storedKey();
        key.setLastUsedAt(NOW.minus(Duration.ofSeconds(30)));
        when(keyRepository.findByKeyPrefix(PREFIX)).thenReturn(Optional.of(key));
        ownerActive(true);

        assertThat(service.authenticate(RAW_KEY)).isPresent();
        verify(keyRepository, never()).touch(anyLong(), any());
    }

    @Test
    void authenticate_malformedKeys_rejectedWithoutDbLookup() {
        for (String bad : new String[] {null, "", "amc_", RAW_KEY + "0", RAW_KEY.toUpperCase(),
                "Bearer " + RAW_KEY, "xyz_" + PREFIX + "_" + SECRET, "amc_" + PREFIX + "-" + SECRET}) {
            assertThat(service.authenticate(bad)).as("key %s", bad).isEmpty();
        }
        verify(keyRepository, never()).findByKeyPrefix(any());
    }

    @Test
    void authenticate_unknownPrefix_rejected() {
        when(keyRepository.findByKeyPrefix(PREFIX)).thenReturn(Optional.empty());

        assertThat(service.authenticate(RAW_KEY)).isEmpty();
    }

    @Test
    void authenticate_wrongSecret_rejected() {
        when(keyRepository.findByKeyPrefix(PREFIX)).thenReturn(Optional.of(storedKey()));

        assertThat(service.authenticate("amc_" + PREFIX + "_" + "e".repeat(64))).isEmpty();
        verify(keyRepository, never()).touch(anyLong(), any());
    }

    @Test
    void authenticate_revokedKey_rejected() {
        AgentKey key = storedKey();
        key.setRevokedAt(NOW.minus(Duration.ofMinutes(5)));
        when(keyRepository.findByKeyPrefix(PREFIX)).thenReturn(Optional.of(key));

        assertThat(service.authenticate(RAW_KEY)).isEmpty();
    }

    @Test
    void authenticate_inactiveOwner_rejected() {
        when(keyRepository.findByKeyPrefix(PREFIX)).thenReturn(Optional.of(storedKey()));
        ownerActive(false);

        assertThat(service.authenticate(RAW_KEY)).isEmpty();
    }

    @Test
    void revoke_ownKey_setsRevokedAt_once() {
        AgentKey key = storedKey();
        when(keyRepository.findByIdAndOwnerUserId(1L, OWNER)).thenReturn(Optional.of(key));

        service.revoke(OWNER, 1L);
        assertThat(key.getRevokedAt()).isEqualTo(NOW);

        //idempotent: the original revocation time is kept
        Instant first = key.getRevokedAt();
        service.revoke(OWNER, 1L);
        assertThat(key.getRevokedAt()).isEqualTo(first);
    }

    @Test
    void revoke_otherUsersKey_looksLikeMissingKey() {
        when(keyRepository.findByIdAndOwnerUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.revoke(99L, 1L)).isInstanceOf(AgentKeyNotFoundException.class);
    }
}
