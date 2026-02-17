package com.load.Ai.service;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.load.Ai.dto.CurrentSessionDTO;
import com.load.Ai.dto.RequestHistoryDTO;
import com.load.Ai.dto.UserDashboardDTO;
import com.load.Ai.dto.UserUsageStatsDTO;
import com.load.Ai.entity.LoginHistory;
import com.load.Ai.entity.RequestLog;
import com.load.Ai.repository.LoginHistoryRepository;
import com.load.Ai.repository.RequestLogRepository;

@Service
public class UserDashboardService {
    
    @Autowired
    private RequestLogRepository requestLogRepository;
    
    @Autowired
    private LoginHistoryRepository loginHistoryRepository;
    
    public UserDashboardDTO getUserDashboard(Long userId) {
        UserDashboardDTO dashboard = new UserDashboardDTO();
        
        dashboard.setUsageStats(getUserUsageStats(userId));
        dashboard.setRequestHistory(getRequestHistory(userId, 20)); // Last 20 requests
        dashboard.setCurrentSession(getCurrentSession(userId));
        
        return dashboard;
    }
    
    public UserUsageStatsDTO getUserUsageStats(Long userId) {
        Long totalRequests = requestLogRepository.countByUserId(userId);
        
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long requestsToday = requestLogRepository.countByUserIdAndRequestTimeAfter(userId, today);
        
        Double avgResponseTime = requestLogRepository.getAverageResponseTimeByUserId(userId);
        Long totalTimeSpent = loginHistoryRepository.getTotalTimeSpentByUserId(userId);
        
        List<Object[]> mostUsed = requestLogRepository.getMostUsedServerByUserId(userId, PageRequest.of(0, 1));
        String mostUsedServer = mostUsed.isEmpty() ? "N/A" : (String) mostUsed.get(0)[0];
        
        return new UserUsageStatsDTO(
                totalRequests,
                requestsToday,
                avgResponseTime != null ? avgResponseTime : 0.0,
                totalTimeSpent != null ? totalTimeSpent : 0L,
                mostUsedServer
        );
    }
    
    public List<RequestHistoryDTO> getRequestHistory(Long userId, int limit) {
        List<RequestLog> logs = requestLogRepository.findByUserIdOrderByRequestTimeDesc(
                userId, 
                PageRequest.of(0, limit)
        );
        
        return logs.stream()
                .map(log -> new RequestHistoryDTO(
                        log.getId(),
                        log.getRequestType(),
                        log.getEndpoint(),
                        log.getServer().getServerName(),
                        log.getResponseTime(),
                        log.getStatusCode(),
                        log.getRequestTime()
                ))
                .collect(Collectors.toList());
    }
    
    public CurrentSessionDTO getCurrentSession(Long userId) {
        // Optional<LoginHistory> activeSession = loginHistoryRepository.findActiveSessionByUserId(userId);
        List<LoginHistory> sessions =
        loginHistoryRepository.findActiveSessionsByUserId(userId);
        
        // if (activeSession.isPresent()) {
        //     LoginHistory session = activeSession.get();
        //     Long duration = Duration.between(session.getLoginTime(), LocalDateTime.now()).getSeconds();
            
        //     return new CurrentSessionDTO(
        //             session.getLoginTime(),
        //             session.getIpAddress(),
        //             "Active",
        //             duration
        //     );
        // }
        
        if (!sessions.isEmpty()) {
    LoginHistory session = sessions.get(0); // latest login
    Long duration = Duration.between(
            session.getLoginTime(),
            LocalDateTime.now()
    ).getSeconds();

    return new CurrentSessionDTO(
            session.getLoginTime(),
            session.getIpAddress(),
            "Active",
            duration
    );
}
        return new CurrentSessionDTO(null, "N/A", "Inactive", 0L);
    }
}