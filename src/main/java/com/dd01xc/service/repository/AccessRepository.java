package com.dd01xc.service.repository;

import com.dd01xc.service.model.AccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
//sql data-repo
@Repository
public interface AccessRepository extends JpaRepository<AccessEvent, Long> {

    List<AccessEvent> findTop10ByOrderByCreatedAtDesc();

    //exists-logic
    long countByStatus(String status);

    @Query(value = """
        SELECT COUNT(*)
        FROM access_events ae
        WHERE ae.status = 'FAILED'
        AND EXISTS (
            SELECT 1
            FROM users u
            WHERE u.email = ae.username_or_email
            OR u.username = ae.username_or_email
        )
        """, nativeQuery = true)
    long countFailedForExistingAccounts();

    //1h sql
    @Query(value = """
        SELECT to_char(gs.bucket_start, 'HH24:MI') AS bucket,
        COUNT(ae.id) FILTER (WHERE ae.status = 'SUCCESS') AS successful,
        COUNT(ae.id) FILTER (
            WHERE ae.status = 'FAILED'
            AND EXISTS (
                SELECT 1
                FROM users u
                WHERE u.email = ae.username_or_email
                OR u.username = ae.username_or_email
            )
        ) AS failed
        FROM generate_series(
            date_trunc('minute', now()) - interval '59 minutes',
            date_trunc('minute', now()),
            interval '10 minute'
        ) AS gs(bucket_start)
        LEFT JOIN access_events ae
            ON ae.created_at >= gs.bucket_start
            AND ae.created_at < gs.bucket_start + interval '10 minute'
        GROUP BY gs.bucket_start
        ORDER BY gs.bucket_start
        """, nativeQuery = true)
    List<Object[]> getStatsLastHourByMinute();

    //24h sql
    @Query(value = """
        SELECT to_char(gs.bucket_start, 'HH24:00') AS bucket,
        COUNT(ae.id) FILTER (WHERE ae.status = 'SUCCESS') AS successful,
        COUNT(ae.id) FILTER (
            WHERE ae.status = 'FAILED'
            AND EXISTS (
                SELECT 1
                FROM users u
                WHERE u.email = ae.username_or_email
                OR u.username = ae.username_or_email
            )
        ) AS failed
        FROM generate_series(
            date_trunc('hour', now()) - interval '23 hours',
            date_trunc('hour', now()),
            interval '1 hour'
        ) AS gs(bucket_start)
        LEFT JOIN access_events ae
            ON ae.created_at >= gs.bucket_start
            AND ae.created_at < gs.bucket_start + interval '1 hour'
        GROUP BY gs.bucket_start
        ORDER BY gs.bucket_start
        """, nativeQuery = true)
    List<Object[]> getStatsLast24HoursByHour();

    //7days sql
    @Query(value = """
        SELECT to_char(gs.bucket_start, 'DD Mon') AS bucket,
        COUNT(ae.id) FILTER (WHERE ae.status = 'SUCCESS') AS successful,
        COUNT(ae.id) FILTER (
            WHERE ae.status = 'FAILED'
            AND EXISTS (
                SELECT 1
                FROM users u
                WHERE u.email = ae.username_or_email
                OR u.username = ae.username_or_email
            )
        ) AS failed
        FROM generate_series(
            date_trunc('day', now()) - interval '6 days',
            date_trunc('day', now()),
            interval '1 day'
        ) AS gs(bucket_start)
        LEFT JOIN access_events ae
            ON ae.created_at >= gs.bucket_start
            AND ae.created_at < gs.bucket_start + interval '1 day'
        GROUP BY gs.bucket_start
        ORDER BY gs.bucket_start
        """, nativeQuery = true)
    List<Object[]> getStatsLast7DaysByDay();


    // Top account 24h sql
    @Query(value = """
        SELECT ae.username_or_email AS account,
        COUNT(*) AS failed_count
        FROM access_events ae
        WHERE ae.status = 'FAILED'
        AND ae.created_at >= now() - interval '24 hours'
        AND EXISTS (
            SELECT 1
            FROM users u
            WHERE u.email = ae.username_or_email
            OR u.username = ae.username_or_email
        )
        GROUP BY ae.username_or_email
        ORDER BY failed_count DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> getTopFailedAccountsLast24Hours();
        //IP-access
    @Query("SELECT a.ipAddress, COUNT(a) FROM AccessEvent a " +
       "WHERE a.status = 'FAILED' " +
       "AND EXISTS (" +
       "SELECT 1 FROM User u " +
       "WHERE u.email = a.usernameOrEmail OR u.username = a.usernameOrEmail) " +
       "GROUP BY a.ipAddress " +
       "ORDER BY COUNT(a) DESC")
    List<Object[]> getTopFailedIps();

    //responce-time
    @Query(value = """
        SELECT to_char(date_trunc('hour', ae.created_at), 'HH24:00') AS bucket,
        MIN(ae.duration_ms) AS min,
        percentile_cont(0.25) WITHIN GROUP (ORDER BY ae.duration_ms) AS val1,
        percentile_cont(0.5) WITHIN GROUP (ORDER BY ae.duration_ms) AS median,
        percentile_cont(0.75) WITHIN GROUP (ORDER BY ae.duration_ms) AS val3,
        MAX(ae.duration_ms) AS max
        FROM access_events ae
        WHERE ae.created_at >= now() - interval '24 hours'
        AND ae.duration_ms IS NOT NULL
        GROUP BY date_trunc('hour', ae.created_at)
        ORDER BY date_trunc('hour', ae.created_at)
        """, nativeQuery=true)
    List<Object[]> getResponceTime();
}
