package com.load.Ai.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.load.Ai.entity.RequestLog;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    
    List<RequestLog> findByUserIdOrderByRequestTimeDesc(Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.user.id = :userId AND r.requestTime >= :startTime")
    Long countByUserIdAndRequestTimeAfter(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(r.responseTime) FROM RequestLog r WHERE r.user.id = :userId")
    Double getAverageResponseTimeByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r.server.serverName, COUNT(r) FROM RequestLog r WHERE r.user.id = :userId GROUP BY r.server.serverName ORDER BY COUNT(r) DESC")
    List<Object[]> getMostUsedServerByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.requestTime >= :startTime")
    Long countRequestsSince(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT AVG(r.responseTime) FROM RequestLog r WHERE r.requestTime >= :startTime")
    Double getAverageResponseTimeSince(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT r.user.id, r.user.username, r.user.email, COUNT(r), AVG(r.responseTime) " +
           "FROM RequestLog r GROUP BY r.user.id, r.user.username, r.user.email")
    List<Object[]> getUserRequestStats();
    
    @Query("SELECT r.server.serverName, COUNT(r) FROM RequestLog r GROUP BY r.server.serverName")
    List<Object[]> getLoadDistribution();
}