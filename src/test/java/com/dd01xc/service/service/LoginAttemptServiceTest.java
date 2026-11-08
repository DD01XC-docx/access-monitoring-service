package com.dd01xc.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dd01xc.service.model.AccessEvent;
import com.dd01xc.service.repository.AccessRepository;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    private static final String EMAIL = "john@example.com";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 12, 0);
    private static final Duration WINDOW = Duration.ofMinutes(15);

    @Mock
    private AccessRepository accessRepository;

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);
        service = new LoginAttemptService(accessRepository, 5, WINDOW, clock);
    }

    private AccessEvent eventMinutesAgo(int minutes) {
        AccessEvent event = new AccessEvent();
        event.setCreatedAt(NOW.minusMinutes(minutes));
        return event;
    }

    //newest first, like the repository query
    private List<AccessEvent> failuresMinutesAgo(int... minutes) {
        return IntStream.of(minutes).mapToObj(this::eventMinutesAgo).toList();
    }

    private void givenNoSuccess() {
        when(accessRepository.findTopByUsernameOrEmailAndStatusOrderByCreatedAtDesc(EMAIL, AccessEvent.STATUS_SUCCESS))
                .thenReturn(Optional.empty());
    }

    private void givenFailuresSince(LocalDateTime since, List<AccessEvent> failures) {
        when(accessRepository.findByUsernameOrEmailAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
                EMAIL, AccessEvent.STATUS_FAILED, since)).thenReturn(failures);
    }

    @Test
    void remainingLock_fourFailures_notLocked() {
        givenNoSuccess();
        givenFailuresSince(NOW.minus(WINDOW), failuresMinutesAgo(1, 2, 3, 4));

        assertThat(service.remainingLock(EMAIL)).isEmpty();
    }

    @Test
    void remainingLock_fiveFailures_lockedUntilFifthNewestLeavesWindow() {
        givenNoSuccess();
        givenFailuresSince(NOW.minus(WINDOW), failuresMinutesAgo(1, 2, 3, 4, 5));

        //5 min ago + 15 min window = 10 min left
        assertThat(service.remainingLock(EMAIL)).contains(Duration.ofMinutes(10));
    }

    @Test
    void remainingLock_sixFailures_usesFifthNewest() {
        givenNoSuccess();
        givenFailuresSince(NOW.minus(WINDOW), failuresMinutesAgo(1, 2, 3, 4, 6, 14));

        assertThat(service.remainingLock(EMAIL)).contains(Duration.ofMinutes(9));
    }

    @Test
    void remainingLock_recentSuccess_countsOnlyFailuresAfterIt() {
        when(accessRepository.findTopByUsernameOrEmailAndStatusOrderByCreatedAtDesc(EMAIL, AccessEvent.STATUS_SUCCESS))
                .thenReturn(Optional.of(eventMinutesAgo(2)));
        givenFailuresSince(NOW.minusMinutes(2), failuresMinutesAgo(1));

        assertThat(service.remainingLock(EMAIL)).isEmpty();
        verify(accessRepository).findByUsernameOrEmailAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
                EMAIL, AccessEvent.STATUS_FAILED, NOW.minusMinutes(2));
    }

    @Test
    void remainingLock_successOlderThanWindow_usesWindowStart() {
        when(accessRepository.findTopByUsernameOrEmailAndStatusOrderByCreatedAtDesc(EMAIL, AccessEvent.STATUS_SUCCESS))
                .thenReturn(Optional.of(eventMinutesAgo(30)));
        when(accessRepository.findByUsernameOrEmailAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
                any(), any(), any())).thenReturn(List.of());

        service.remainingLock(EMAIL);

        verify(accessRepository).findByUsernameOrEmailAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(
                EMAIL, AccessEvent.STATUS_FAILED, NOW.minus(WINDOW));
    }
}
