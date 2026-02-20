package com.dd01xc.service.repository;

import com.dd01xc.service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query(value = """
    SELECT CONCAT(COALESCE(role, 'UNKNOWN_ROLE'), ' • ', COALESCE(status, 'UNKNOWN_STATUS')) AS bucket,
    COUNT(*) AS total
    FROM users
    GROUP BY COALESCE(role, 'UNKNOWN_ROLE'), COALESCE(status, 'UNKNOWN_STATUS')
    ORDER BY total DESC
    """, nativeQuery = true)
List<Object[]> usersByStat();

}