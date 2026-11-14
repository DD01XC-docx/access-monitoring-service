package com.dd01xc.service.init;

import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

//one-off: access_events.created_at was "timestamp" (JVM local time); converts it to timestamptz.
//ddl-auto=update never changes column types. Replace with a Flyway migration once Flyway is added.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TimestampMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TimestampMigration.class);

    private final JdbcTemplate jdbc;

    public TimestampMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        String type = jdbc.query("""
            SELECT data_type FROM information_schema.columns
            WHERE table_name = 'access_events' AND column_name = 'created_at'
            """, rs -> rs.next() ? rs.getString(1) : null);
        if (!"timestamp without time zone".equals(type)) {
            return;
        }
        //old rows were written with LocalDateTime.now(), i.e. in this JVM's zone
        String zone = ZoneId.systemDefault().getId();
        jdbc.execute("ALTER TABLE access_events ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE '"
            + zone.replace("'", "") + "'");
        log.info("access_events.created_at converted to timestamptz (old values read as {})", zone);
    }
}
