package com.load.Ai.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;

    private LocalDateTime lastActiveTime;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    // Getters and Setters

    public SessionStatus getStatus() {
        return status;
    }

    public Server getServer() {
        return server;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setServer(Server server) {
       this.server = server;
   }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    @Column(name = "selection_method")
private String selectionMethod;
public String getSelectionMethod() {
    return selectionMethod;
}

public void setSelectionMethod(String selectionMethod) {
    this.selectionMethod = selectionMethod;
}
}
