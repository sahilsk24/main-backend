package com.load.Ai.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.load.Ai.entity.Server;
import com.load.Ai.entity.SessionStatus;
import com.load.Ai.entity.User;
import com.load.Ai.entity.UserSession;
import com.load.Ai.repository.UserRepository;
import com.load.Ai.repository.UserSessionRepository;

@Service
public class SessionService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private LoadBalancerService loadBalancerService;

    // =========================================
    // Active / Inactive User Count
    // =========================================
    // public long getActiveUserCount() {
    //     return userSessionRepository.countByStatus(SessionStatus.ACTIVE);
    // }
    public long getActiveUserCount() {
    return userSessionRepository
            .countActiveNormalUsers(SessionStatus.ACTIVE);
}

    public long getInactiveUserCount() {
        return userSessionRepository.countByStatus(SessionStatus.INACTIVE);
    }

    // =========================================
    // Dynamic Server Load (BEST PRACTICE)
    // =========================================
    public long getServerLoad(Long serverId) {
        return userSessionRepository
                .countByServerIdAndStatus(serverId, SessionStatus.ACTIVE);
    }

    // =========================================
    // Assign server when user logs in


@Autowired
private MLLoadBalancerService mlLoadBalancerService;
@Autowired
private UserRepository userRepository;
@Transactional
public Server assignServerToUser(Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Check existing active session
    UserSession existing = userSessionRepository
            .findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
            .orElse(null);

    if (existing != null) {
        existing.setLastActiveTime(LocalDateTime.now());
        userSessionRepository.save(existing);
        return existing.getServer();
    }

    Server server;
    String selectionMethod;

    // 🔥 Decide ML vs Rule-Based
    if (mlLoadBalancerService.isMLServiceHealthy()) {

        server = mlLoadBalancerService
                .selectBestServerUsingML(userId);

        selectionMethod = "AI_ML";

    } else {

        server = loadBalancerService.selectBestServer();

        selectionMethod = "RULE_BASED";
    }

    // Create new session
    UserSession session = new UserSession();
    session.setUserId(userId);
    session.setServer(server);
    session.setLastActiveTime(LocalDateTime.now());
    session.setStatus(SessionStatus.ACTIVE);
    session.setSelectionMethod(selectionMethod); // ✅ store method

    userSessionRepository.save(session);

    // Increase load
    loadBalancerService.updateServerLoad(server.getId(), true);

    return server;
}

    // =========================================
    // Update activity on request
    // =========================================
    @Transactional
    public void refreshUserActivity(Long userId) {

        userSessionRepository
                .findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    session.setLastActiveTime(LocalDateTime.now());
                    userSessionRepository.save(session);
                });
    }

    // =========================================
    // Logout user manually
    // =========================================
   @Transactional
public void logoutUser(Long userId) {

    userSessionRepository
            .findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
            .ifPresent(session -> {

                // 🔥 Decrease server load
                loadBalancerService.updateServerLoad(
                        session.getServer().getId(),
                        false
                );

                session.setStatus(SessionStatus.INACTIVE);
                userSessionRepository.save(session);
            });
}

    // =========================================
    // Auto cleanup (60 sec timeout)
    // =========================================
   @Scheduled(fixedRate = 15000) // runs every 15 seconds
@Transactional
public void cleanupInactiveSessions() {

    LocalDateTime timeout = LocalDateTime.now().minusMinutes(5);

    List<UserSession> expiredSessions =
            userSessionRepository
                    .findByStatusAndLastActiveTimeBefore(
                            SessionStatus.ACTIVE,
                            timeout);

    // for (UserSession session : expiredSessions) {
    //     session.setStatus(SessionStatus.INACTIVE);
    //     userSessionRepository.save(session);
    //     System.out.println("Session expired for user: " + session.getUserId());
    // }
    for (UserSession session : expiredSessions) {

    // 🔥 Decrease load when session auto-expires
    loadBalancerService.updateServerLoad(
            session.getServer().getId(),
            false
    );

    session.setStatus(SessionStatus.INACTIVE);
    userSessionRepository.save(session);


    System.out.println("Session expired for user: " + session.getUserId());
}
}
}