package com.dd01xc.service.agent.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dd01xc.service.agent.model.AgentKey;
import com.dd01xc.service.agent.model.AgentKeyDTOs.CreatedKey;
import com.dd01xc.service.agent.model.AgentKeyDTOs.KeyView;
import com.dd01xc.service.agent.repository.AgentKeyRepository;
import com.dd01xc.service.agent.security.AgentPrincipal;
import com.dd01xc.service.model.User;
import com.dd01xc.service.repository.UserRepository;
import com.dd01xc.service.service.exception.AgentKeyLimitException;
import com.dd01xc.service.service.exception.AgentKeyNotFoundException;

//key format: amc_<12 hex prefix>_<64 hex secret>; prefix finds the row, secret is checked by hash
@Service
public class AgentKeyService {

    public static final int MAX_ACTIVE_KEYS = 10;
    private static final String KEY_TAG = "amc_";
    private static final int PREFIX_BYTES = 6;
    private static final int SECRET_BYTES = 32;
    private static final Pattern KEY_FORMAT = Pattern.compile("^amc_([0-9a-f]{12})_([0-9a-f]{64})$");
    //last_used_at is informational, one write per minute per key is enough
    private static final Duration TOUCH_INTERVAL = Duration.ofMinutes(1);
    private static final HexFormat HEX = HexFormat.of();

    private final AgentKeyRepository keyRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public AgentKeyService(AgentKeyRepository keyRepository, UserRepository userRepository) {
        this(keyRepository, userRepository, Clock.systemUTC());
    }

    AgentKeyService(AgentKeyRepository keyRepository, UserRepository userRepository, Clock clock) {
        this.keyRepository = keyRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public CreatedKey create(Long ownerUserId, String name) {
        if (keyRepository.countByOwnerUserIdAndRevokedAtIsNull(ownerUserId) >= MAX_ACTIVE_KEYS) {
            throw new AgentKeyLimitException("At most " + MAX_ACTIVE_KEYS + " active agent keys per user");
        }
        String prefix = HEX.formatHex(randomBytes(PREFIX_BYTES));
        String secret = HEX.formatHex(randomBytes(SECRET_BYTES));

        AgentKey key = new AgentKey();
        key.setOwnerUserId(ownerUserId);
        key.setName(name.strip());
        key.setKeyPrefix(prefix);
        key.setKeyHash(sha256Hex(secret));
        key.setCreatedAt(clock.instant());
        keyRepository.save(key);

        return new CreatedKey(key.getId(), key.getName(), prefix, KEY_TAG + prefix + "_" + secret, key.getCreatedAt());
    }

    public List<KeyView> list(Long ownerUserId) {
        return keyRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId).stream().map(KeyView::from).toList();
    }

    //idempotent; another user's key is reported as not found (no enumeration)
    @Transactional
    public void revoke(Long ownerUserId, Long keyId) {
        AgentKey key = keyRepository.findByIdAndOwnerUserId(keyId, ownerUserId)
            .orElseThrow(() -> new AgentKeyNotFoundException("Agent key not found"));
        if (!key.isRevoked()) {
            key.setRevokedAt(clock.instant());
        }
    }

    //empty for every failure reason, so callers can't tell them apart
    @Transactional
    public Optional<AgentPrincipal> authenticate(String rawKey) {
        Matcher m = rawKey == null ? null : KEY_FORMAT.matcher(rawKey);
        if (m == null || !m.matches()) {
            return Optional.empty();
        }
        //hash before the lookup so unknown prefixes cost the same as wrong secrets
        byte[] presented = sha256(m.group(2));

        Optional<AgentKey> found = keyRepository.findByKeyPrefix(m.group(1));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        AgentKey key = found.get();
        //constant-time compare
        if (!MessageDigest.isEqual(presented, HEX.parseHex(key.getKeyHash())) || key.isRevoked()) {
            return Optional.empty();
        }
        //disabled or deleted owner: all their keys stop working
        if (!userRepository.findById(key.getOwnerUserId()).map(User::isActive).orElse(false)) {
            return Optional.empty();
        }

        Instant now = clock.instant();
        if (key.getLastUsedAt() == null || key.getLastUsedAt().isBefore(now.minus(TOUCH_INTERVAL))) {
            keyRepository.touch(key.getId(), now);
        }
        return Optional.of(new AgentPrincipal(key.getId(), key.getOwnerUserId(), key.getName(), key.getKeyPrefix()));
    }

    private byte[] randomBytes(int n) {
        byte[] bytes = new byte[n];
        random.nextBytes(bytes);
        return bytes;
    }

    //plain SHA-256 is enough: the secret is 256 random bits, not a guessable password
    static String sha256Hex(String secret) {
        return HEX.formatHex(sha256(secret));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
