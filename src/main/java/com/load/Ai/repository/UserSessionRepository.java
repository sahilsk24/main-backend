package com.load.Ai.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.load.Ai.entity.SessionStatus;
import com.load.Ai.entity.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    List<UserSession> findByStatusAndLastActiveTimeBefore(
            SessionStatus status,
            LocalDateTime time);

    Long countByServerIdAndStatus(Long serverId, SessionStatus status);
    Long countByStatus(SessionStatus status);  
    @Query("""
    SELECT COUNT(us)
    FROM UserSession us
    JOIN User u ON us.userId = u.id
    WHERE us.status = :status
    AND u.role = 'USER'
""")
Long countActiveNormalUsers(@Param("status") SessionStatus status);
}