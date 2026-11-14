package com.dd01xc.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dd01xc.service.repository.AccessRepository;
import com.dd01xc.service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AccessStatServiceTest {

    @Mock
    private AccessRepository accessRepository;
    @Mock
    private UserRepository userRepository;

    private AccessStatService service;

    @BeforeEach
    void setUp() {
        service = new AccessStatService(accessRepository, userRepository);
    }

    //rows: [bucket, successful, failed]
    private static Object[] row(String bucket, long successful, long failed) {
        return new Object[] {bucket, successful, failed};
    }

    @Test
    void getSlaStatus_usesRowsOfRequestedRange() {
        when(accessRepository.getStatsLastHourByMinute()).thenReturn(List.of(
            row("2026-01-01T12:00:00Z", 2, 1),
            row("2026-01-01T12:10:00Z", 0, 0)));

        Map<String, Object> sla = service.getSlaStatus("1h");

        //2 of 3 -> 67%
        assertThat(sla.get("series")).isEqualTo(List.of(67));
    }

    @Test
    void getSlaStatus_noLogins_returnsEmptySeries() {
        when(accessRepository.getStatsLast24HoursByHour()).thenReturn(List.<Object[]>of(row("2026-01-01T12:00:00Z", 0, 0)));

        assertThat(service.getSlaStatus("24h").get("series")).isEqualTo(List.of());
    }

    @Test
    void getSlaStatus_unknownRange_fallsBackTo24h() {
        when(accessRepository.getStatsLast24HoursByHour()).thenReturn(List.<Object[]>of(row("2026-01-01T12:00:00Z", 1, 0)));

        assertThat(service.getSlaStatus("bogus").get("series")).isEqualTo(List.of(100));
    }
}
