package com.load.Ai.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.load.Ai.entity.LoginHistory;
import com.load.Ai.entity.LoginStatus;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId, Pageable pageable);
    
   @Query(
  "SELECT lh FROM LoginHistory lh " +
  "WHERE lh.user.id = :userId AND lh.logoutTime IS NULL " +
  "ORDER BY lh.loginTime DESC"
)
List<LoginHistory> findActiveSessionsByUserId(@Param("userId") Long userId);

    
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.user.id = :userId AND lh.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") LoginStatus status);
    
    @Query("SELECT COUNT(DISTINCT lh.user.id) FROM LoginHistory lh WHERE lh.loginTime >= :startTime AND lh.status = 'SUCCESS'")
    Long countActiveUsersSince(@Param("startTime") LocalDateTime startTime);
    
    @Query(
  "SELECT SUM(" +
  "TIMESTAMPDIFF(SECOND, lh.loginTime, COALESCE(lh.logoutTime, CURRENT_TIMESTAMP))" +
  ") FROM LoginHistory lh " +
  "WHERE lh.user.id = :userId AND lh.status = 'SUCCESS'"
)
Long getTotalTimeSpentByUserId(@Param("userId") Long userId);

}
