package com.load.Ai.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.load.Ai.dto.AccountingReportDTO;
import com.load.Ai.dto.AdminDashboardDTO;
import com.load.Ai.dto.LoadDistributionDTO;
import com.load.Ai.dto.ServerHealthDTO;
import com.load.Ai.dto.SystemOverviewDTO;
import com.load.Ai.dto.UserStatsDTO;
import com.load.Ai.entity.ServerStatus;
import com.load.Ai.repository.LoginHistoryRepository;
import com.load.Ai.repository.RequestLogRepository;
import com.load.Ai.repository.ServerRepository;
import com.load.Ai.repository.UserRepository;

@Service
public class AdminDashboardService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ServerRepository serverRepository;
    
    @Autowired
    private RequestLogRepository requestLogRepository;
    
    @Autowired
    private LoginHistoryRepository loginHistoryRepository;
    
    public AdminDashboardDTO getAdminDashboard() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();
        
        dashboard.setUserStats(getUserStats());
        dashboard.setServerHealthList(getServerHealth());
        dashboard.setLoadDistribution(getLoadDistribution());
        dashboard.setSystemOverview(getSystemOverview());
        
        return dashboard;
    }
    
    @Autowired
private SessionService sessionService;
    public UserStatsDTO getUserStats() {
      Long totalUsers = userRepository.count();
Long activeUsers = sessionService.getActiveUserCount();
Long inactiveUsers = Math.max(0, totalUsers - activeUsers);

        LocalDateTime today =
        LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        // Count users created today
        Long newUsersToday = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(today))
                .count();
        
        return new UserStatsDTO(totalUsers, activeUsers, inactiveUsers, newUsersToday);
    }
    
    public List<ServerHealthDTO> getServerHealth() {
        return serverRepository.findAll().stream()
                .map(server -> new ServerHealthDTO(
                        server.getId(),
                        server.getServerName(),
                        server.getServerIp(),
                        server.getStatus().name(),
                        server.getCpuUsage(),
                        server.getMemoryUsage(),
                        sessionService.getServerLoad(server.getId()),
                        server.getTotalRequestsHandled()
                ))
                .collect(Collectors.toList());
    }
    
    // public List<LoadDistributionDTO> getLoadDistribution() {
    //     List<Object[]> distribution = requestLogRepository.getLoadDistribution();
    //     Long totalRequests = distribution.stream()
    //             .mapToLong(obj -> ((Number) obj[1]).longValue())
    //             .sum();
        
    //     List<LoadDistributionDTO> result = new ArrayList<>();
    //     for (Object[] obj : distribution) {
    //         String serverName = (String) obj[0];
    //         Integer requestsHandled = ((Number) obj[1]).intValue();
    //         Double loadPercentage = totalRequests > 0 ? (requestsHandled * 100.0) / totalRequests : 0.0;
            
    //         result.add(new LoadDistributionDTO(serverName, requestsHandled, loadPercentage));
    //     }
        
    //     return result;
    // }
     public List<LoadDistributionDTO> getLoadDistribution() {
        List<Object[]> distribution = requestLogRepository.getLoadDistribution();
        Long totalRequests = distribution.stream()
                .mapToLong(obj -> ((Number) obj[1]).longValue())
                .sum();
        
        List<LoadDistributionDTO> result = new ArrayList<>();
        for (Object[] obj : distribution) {
            String serverName = (String) obj[0];
            Integer requestsHandled = ((Number) obj[1]).intValue();
            Double loadPercentage = totalRequests > 0 ? (requestsHandled * 100.0) / totalRequests : 0.0;
            
            result.add(new LoadDistributionDTO(serverName, requestsHandled, loadPercentage));
        }
        
        return result;
    }
    
    
    public SystemOverviewDTO getSystemOverview() {
        Integer totalServers = (int) serverRepository.count();
        Integer activeServers = serverRepository.countByStatus(ServerStatus.ACTIVE).intValue();
        Integer overloadedServers = serverRepository.countByStatus(ServerStatus.OVERLOADED).intValue();
        Integer inactiveServers = serverRepository.countByStatus(ServerStatus.INACTIVE).intValue();
        
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long totalRequestsToday = requestLogRepository.countRequestsSince(today);
        Double averageResponseTime = requestLogRepository.getAverageResponseTimeSince(today);
        
        return new SystemOverviewDTO(
                totalServers,
                activeServers,
                overloadedServers,
                inactiveServers,
                totalRequestsToday,
                averageResponseTime != null ? averageResponseTime : 0.0
        );
    }
    
    public List<AccountingReportDTO> getAccountingReports() {
        List<Object[]> stats = requestLogRepository.getUserRequestStats();
        List<AccountingReportDTO> reports = new ArrayList<>();
        
        for (Object[] stat : stats) {
            Long userId = ((Number) stat[0]).longValue();
            String username = (String) stat[1];
            String email = (String) stat[2];
            Long totalRequests = ((Number) stat[3]).longValue();
            Double avgResponseTime = stat[4] != null ? ((Number) stat[4]).doubleValue() : 0.0;
            
            Long totalTimeSpent = loginHistoryRepository.getTotalTimeSpentByUserId(userId);
            Long loginCount = loginHistoryRepository.countByUserIdAndStatus(userId, 
                    com.load.Ai.entity.LoginStatus.SUCCESS);
            
            reports.add(new AccountingReportDTO(
                    userId,
                    username,
                    email,
                    totalRequests,
                    avgResponseTime,
                    totalTimeSpent != null ? totalTimeSpent : 0L,
                    loginCount.intValue()
            ));
        }
        
        return reports;
    }
}