package com.load.Ai.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.load.Ai.dto.AccountingReportDTO;
import com.load.Ai.dto.AdminDashboardDTO;
import com.load.Ai.dto.LoadDistributionDTO;
import com.load.Ai.dto.ServerHealthDTO;
import com.load.Ai.dto.SystemOverviewDTO;
import com.load.Ai.dto.UserStatsDTO;
import com.load.Ai.entity.RequestLog;
import com.load.Ai.repository.RequestLogRepository;
import com.load.Ai.service.AdminDashboardService;
import com.load.Ai.service.MLLoadBalancerService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    @Autowired
    private AdminDashboardService adminDashboardService;
    
    /**
     * Get complete admin dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        try {
            AdminDashboardDTO dashboard = adminDashboardService.getAdminDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get user statistics
     */
    @GetMapping("/users/stats")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        try {
            UserStatsDTO stats = adminDashboardService.getUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get server health monitoring data
     */
    @GetMapping("/servers/health")
    public ResponseEntity<List<ServerHealthDTO>> getServerHealth() {
        try {
            List<ServerHealthDTO> serverHealth = adminDashboardService.getServerHealth();
            return ResponseEntity.ok(serverHealth);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get load distribution across servers
     */
    @GetMapping("/servers/load-distribution")
    public ResponseEntity<List<LoadDistributionDTO>> getLoadDistribution() {
        try {
            List<LoadDistributionDTO> distribution = adminDashboardService.getLoadDistribution();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get system overview
     */
    @GetMapping("/system/overview")
    public ResponseEntity<SystemOverviewDTO> getSystemOverview() {
        try {
            SystemOverviewDTO overview = adminDashboardService.getSystemOverview();
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get accounting reports (user-wise usage)
     */
    @GetMapping("/reports/accounting")
    public ResponseEntity<List<AccountingReportDTO>> getAccountingReports() {
        try {
            List<AccountingReportDTO> reports = adminDashboardService.getAccountingReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
   @Autowired
private MLLoadBalancerService mlLoadBalancerService;

@GetMapping("/servers/ai-decision")
public ResponseEntity<?> getMLServerDecision() {

    try {

        Long demoUserId = 1L;

        Map<String, Object> result =
                mlLoadBalancerService.getMLDecision(demoUserId);

        return ResponseEntity.ok(result);

    } catch (Exception e) {

        e.printStackTrace();

        return ResponseEntity.badRequest()
                .body("ML decision error: " + e.getMessage());
    }
}

@Autowired
private RequestLogRepository requestLogRepository;

@GetMapping("/request-logs/export")
public ResponseEntity<?> exportRequestLogs(
        @RequestParam(defaultValue = "350") int limit) {
    
    // Get all logs, excluding Round-Robin test data
    List<RequestLog> allLogs = requestLogRepository.findAll(
        Sort.by("requestTime").ascending()
    );
    
    // Filter OUT any Round-Robin test data
    List<RequestLog> originalLogs = allLogs.stream()
        .filter(log -> log.getEndpoint() != null && 
                      !log.getEndpoint().contains("-roundrobin") &&
                      !log.getEndpoint().contains("-ml"))
        .limit(limit)
        .collect(Collectors.toList());
    
    List<Map<String, Object>> data = new ArrayList<>();
    
    for (int i = 0; i < originalLogs.size(); i++) {
        RequestLog log = originalLogs.get(i);
        Map<String, Object> record = new HashMap<>();
        record.put("requestNumber", i + 1);
        record.put("timestamp", log.getRequestTime().toString());
        record.put("server", log.getServer().getServerName());
        record.put("responseTime", log.getResponseTime());
        record.put("statusCode", log.getStatusCode());
        record.put("endpoint", log.getEndpoint());
        record.put("requestType", log.getRequestType());
        data.add(record);
    }
    
    return ResponseEntity.ok(data);
}
}
