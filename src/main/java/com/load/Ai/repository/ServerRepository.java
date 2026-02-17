package com.load.Ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
