package com.load.Ai.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.load.Ai.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.username = :identifier) AND u.deletedAt IS NULL")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);
    
    Boolean existsByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    // For soft delete - find only non-deleted users
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start")
Long countUsersCreatedAfter(@Param("start") LocalDateTime start);
}