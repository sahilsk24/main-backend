package com.load.Ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.load.Ai.entity.Server;
import com.load.Ai.entity.ServerStatus;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    
    List<Server> findByStatus(ServerStatus status);
    
    @Query("SELECT s FROM Server s ORDER BY s.currentLoad ASC")
    List<Server> findServersOrderedByLoad();
    
    @Query("SELECT COUNT(s) FROM Server s WHERE s.status = :status")
    Long countByStatus(ServerStatus status);
    
@Modifying(clearAutomatically = true)
@Query("UPDATE Server s SET s.currentLoad = s.currentLoad + 1 WHERE s.id = :id")
int incrementLoad(@Param("id") Long id);

@Modifying(clearAutomatically = true)
@Query("UPDATE Server s SET s.currentLoad = CASE WHEN s.currentLoad > 0 THEN s.currentLoad - 1 ELSE 0 END WHERE s.id = :id")
int decrementLoad(@Param("id") Long id);




}
