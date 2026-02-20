package com.dd01xc.service.repository;

import com.dd01xc.service.model.AccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
//sql data-repo
@Repository
public interface AccessRepository extends JpaRepository<AccessEvent, Long> {

    long countByStatus(String status);

    //1h sql
    @Query(value = """
        SELECT to_char(gs.minute, 'HH24:MI') AS bucket,
        COALESCE(SUM(CASE WHEN ae.status = 'SUCCESS' THEN 1 END), 0) AS successful,
        COALESCE(SUM(CASE WHEN ae.status = 'FAILED' THEN 1 END), 0) AS failed
        FROM generate_series(
        date_trunc('minute', now()) - interval '59 minutes',
        date_trunc('minute', now()), interval '10 minute') AS gs(minute)
        LEFT JOIN access_events ae
        ON ae.created_at >= gs.minute
        AND ae.created_at < gs.minute + interval '10 minute'
        AND ae.created_at >= date_trunc('minute', now()) - interval '59 minutes'
        AND ae.created_at < date_trunc('minute', now()) + interval '1 minute'
        GROUP BY gs.minute
        ORDER BY gs.minute
        """, nativeQuery = true)
    List<Object[]> getStatsLastHourByMinute();

    //24h sql
    @Query(value = """
        SELECT to_char(gs.hour, 'HH24:00') AS bucket,
        COALESCE(SUM(CASE WHEN ae.status = 'SUCCESS' THEN 1 END), 0) AS successful,
        COALESCE(SUM(CASE WHEN ae.status = 'FAILED' THEN 1 END), 0) AS failed
        FROM generate_series(
        date_trunc('hour', now()) - interval '23 hours',
        date_trunc('hour', now()),
        interval '1 hour'
        ) AS gs(hour)
        LEFT JOIN access_events ae
        ON date_trunc('hour', ae.created_at) = gs.hour
        AND ae.created_at >= date_trunc('hour', now()) - interval '23 hours'
        AND ae.created_at <  date_trunc('hour', now()) + interval '1 hour'
        GROUP BY gs.hour
        ORDER BY gs.hour
        """, nativeQuery = true)
    List<Object[]> getStatsLast24HoursByHour();

    //7days sql
    @Query(value = """
        SELECT to_char(gs.day, 'DD Mon') AS bucket,
        COALESCE(SUM(CASE WHEN ae.status = 'SUCCESS' THEN 1 END), 0) AS successful,
        COALESCE(SUM(CASE WHEN ae.status = 'FAILED' THEN 1 END), 0) AS failed
        FROM generate_series(
        date_trunc('day', now()) - interval '6 days',
        date_trunc('day', now()),
        interval '1 day'
        ) AS gs(day)
        LEFT JOIN access_events ae
        ON date_trunc('day', ae.created_at) = gs.day
        AND ae.created_at >= date_trunc('day', now()) - interval '6 days'
        AND ae.created_at <  date_trunc('day', now()) + interval '1 day'
        GROUP BY gs.day
        ORDER BY gs.day
        """, nativeQuery = true)
    List<Object[]> getStatsLast7DaysByDay();


    // Top account 24h sql
    @Query(value = """
        SELECT ae.username_or_email AS account,
        COUNT(*) AS failed_count
        FROM access_events ae
        WHERE ae.status = 'FAILED'
        AND ae.created_at >= now() - interval '24 hours'
        GROUP BY ae.username_or_email
        ORDER BY failed_count DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> getTopFailedAccountsLast24Hours();
}