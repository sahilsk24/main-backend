package com.load.Ai.controller;




import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.load.Ai.dto.MessageResponse;
import com.load.Ai.entity.RequestLog;
import com.load.Ai.entity.Server;
import com.load.Ai.entity.User;
import com.load.Ai.repository.RequestLogRepository;
import com.load.Ai.repository.UserRepository;
import com.load.Ai.security.UserDetailsImpl;
import com.load.Ai.service.LoadBalancerService;
import com.load.Ai.service.MLLoadBalancerService;

@RestController
@RequestMapping("/api/request")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class RequestController {
    
    @Autowired
    private LoadBalancerService loadBalancerService;
    
    @Autowired
    private MLLoadBalancerService mlLoadBalancerService;
    
    @Autowired
    private RequestLogRepository requestLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private final Random random = new Random();
    
    /**
     * Send a request - AI ML model selects best server
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, String> requestData) {
        try {
            Long userId = getCurrentUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Use ML Load Balancer to select best server
            Server selectedServer;
            String selectionMethod;
            
            if (mlLoadBalancerService.isMLServiceHealthy()) {
                selectedServer = mlLoadBalancerService.selectBestServerUsingML(userId);
                selectionMethod = "AI/ML Algorithm";
            } else {
                // Fallback to traditional load balancer
                selectedServer = loadBalancerService.selectBestServer();
                selectionMethod = "Traditional Load Balancer (ML service unavailable)";
            }
            
            // Increment server load
            loadBalancerService.updateServerLoad(selectedServer.getId(), true);
            
            // Simulate request processing time
            int responseTime = 50 + random.nextInt(450); // 50-500ms
            Thread.sleep(responseTime);
            
            // Log the request
            RequestLog log = new RequestLog();
            log.setUser(user);
            log.setServer(selectedServer);
            log.setRequestType(requestData.getOrDefault("type", "POST"));
            log.setEndpoint(requestData.getOrDefault("endpoint", "/api/data"));
            log.setResponseTime(responseTime);
            log.setStatusCode(200);
            log.setRequestTime(LocalDateTime.now());
            requestLogRepository.save(log);
            
            // Decrement server load
            loadBalancerService.updateServerLoad(selectedServer.getId(), false);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request processed successfully");
            response.put("assignedServer", selectedServer.getServerName());
            response.put("serverId", selectedServer.getId());
            response.put("serverIp", selectedServer.getServerIp());
            response.put("serverPort", selectedServer.getServerPort());
            response.put("responseTime", responseTime + "ms");
            response.put("selectionMethod", selectionMethod);
            response.put("status", "Success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error processing request: " + e.getMessage()));
        }
    }
    
    /**
     * Check ML service health
     */
    @GetMapping("/ml-status")
    public ResponseEntity<?> checkMLStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean healthy = mlLoadBalancerService.isMLServiceHealthy();
        status.put("mlServiceHealthy", healthy);
        status.put("status", healthy ? "Online" : "Offline");
        status.put("message", healthy ? "ML Load Balancer is active" : "Using fallback load balancer");
        return ResponseEntity.ok(status);
    }
    
    /**
     * Get assigned server info for current request
     */
    @GetMapping("/server-info")
    public ResponseEntity<?> getServerInfo() {
        try {
            Long userId = getCurrentUserId();
            Server server;
            
            if (mlLoadBalancerService.isMLServiceHealthy()) {
                server = mlLoadBalancerService.selectBestServerUsingML(userId);
            } else {
                server = loadBalancerService.selectBestServer();
            }
            
            Map<String, Object> info = new HashMap<>();
            info.put("serverId", server.getId());
            info.put("serverName", server.getServerName());
            info.put("serverIp", server.getServerIp());
            info.put("serverPort", server.getServerPort());
            info.put("status", server.getStatus().name());
            info.put("currentLoad", server.getCurrentLoad());
            info.put("cpuUsage", server.getCpuUsage() + "%");
            info.put("memoryUsage", server.getMemoryUsage() + "%");
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to get current logged-in user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}