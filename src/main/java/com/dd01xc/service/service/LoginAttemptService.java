package com.dd01xc.service.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dd01xc.service.model.AccessEvent;
import com.dd01xc.service.repository.AccessRepository;

//per-email lockout based on access_events; unknown emails lock the same way (no enumeration)
@Service
public class LoginAttemptService {

    private final AccessRepository accessRepository;
    private final int maxAttempts;
    private final Duration window;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(AccessRepository accessRepository,
            @Value("${auth.lockout.max-attempts:5}") int maxAttempts,
            @Value("${auth.lockout.window-minutes:15}") long windowMinutes) {
        this(accessRepository, maxAttempts, Duration.ofMinutes(windowMinutes), Clock.systemDefaultZone());
    }

    LoginAttemptService(AccessRepository accessRepository, int maxAttempts, Duration window, Clock clock) {
        this.accessRepository = accessRepository;
        this.maxAttempts = maxAttempts;
        this.window = window;
        this.clock = clock;
    }

    //empty if not locked, otherwise time left until unlock
    public Optional<Duration> remainingLock(String email) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime since = now.minus(window);

        //failures before the last successful login do not count
        Optional<LocalDateTime> lastSuccess = accessRepository.findLastEventTime(email, AccessEvent.STATUS_SUCCESS);
        if (lastSuccess.isPresent() && lastSuccess.get().isAfter(since)) {
            since = lastSuccess.get();
        }

        List<AccessEvent> failures = accessRepository.findEventsAfter(email, AccessEvent.STATUS_FAILED, since);
        if (failures.size() < maxAttempts) {
            return Optional.empty();
        }

        //unlocks when the maxAttempts-th newest failure leaves the window
        LocalDateTime unlockAt = failures.get(maxAttempts - 1).getCreatedAt().plus(window);
        return Optional.of(Duration.between(now, unlockAt));
    }
}
