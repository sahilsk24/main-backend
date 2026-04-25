package com.load.Ai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.load.Ai.entity.Server;
import com.load.Ai.entity.ServerStatus;
import com.load.Ai.entity.SessionStatus;
import com.load.Ai.repository.RequestLogRepository;
import com.load.Ai.repository.ServerRepository;
import com.load.Ai.repository.UserSessionRepository;

@Service
public class MLLoadBalancerService {
    
    @Autowired
    private ServerRepository serverRepository;
    
    @Autowired
    private RequestLogRepository requestLogRepository;
//    @Autowired
// private SessionService sessionService;
@Autowired
private UserSessionRepository userSessionRepository;
    
    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;
    
    private final WebClient webClient;
    
    public MLLoadBalancerService() {
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * Use ML model to predict best server
     */
    public Server selectBestServerUsingML(Long userId) {
        try {
            // Get all active servers
            // List<Server> servers = serverRepository.findAll();
            List<Server> servers = serverRepository.findByStatus(ServerStatus.ACTIVE);

            
            if (servers.isEmpty()) {
                throw new RuntimeException("No servers available");
            }
            
            // Get user statistics
            Map<String, Object> userStats = getUserStats(userId);
            
            // Prepare server data for ML model
            List<Map<String, Object>> serverData = servers.stream()
                    .map(this::convertServerToMLFormat)
                    .collect(Collectors.toList());
            
            // Prepare request payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("user", userStats);
            requestPayload.put("servers", serverData);
            
            // Call Flask ML service
            Map<String, Object> mlResponse = webClient.post()
                    .uri(mlServiceUrl + "/predict-server")
                    .bodyValue(requestPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (mlResponse == null) {
                throw new RuntimeException("ML service returned null response");
            }
            
            // Get selected server ID from ML response
            Long selectedServerId = ((Number) mlResponse.get("selected_server")).longValue();
            
            // Find and return the selected server
            // return serverRepository.findById(selectedServerId)
            //         .orElseThrow(() -> new RuntimeException("Selected server not found"));
            

             Server selected = serverRepository.findById(selectedServerId)
                         .orElseThrow(() -> new RuntimeException("Selected server not found"));

             if (selected.getStatus() != ServerStatus.ACTIVE) {
                      throw new RuntimeException("ML selected inactive server");
                      }

                 return selected;


        } catch (Exception e) {
            System.err.println("ML service error: " + e.getMessage());
            // Fallback to simple load balancing if ML fails
            return serverRepository.findServersOrderedByLoad().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No servers available"));
        }
    }
    
    /**
     * Get user statistics for ML model
     */
    private Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("user_id", userId);
        
        Long totalRequests = requestLogRepository.countByUserId(userId);
        Double avgResponseTime = requestLogRepository.getAverageResponseTimeByUserId(userId);
        
        stats.put("avg_requests", totalRequests != null ? totalRequests : 0);
        stats.put("avg_response", avgResponseTime != null ? avgResponseTime : 0.0);
        
        return stats;
    }
    
    /**
     * Convert Server entity to ML model format
     */
    private Map<String, Object> convertServerToMLFormat(Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("server_id", server.getId());
        data.put("server_name", server.getServerName());
        data.put("status", server.getStatus().toString());
        data.put("cpu", server.getCpuUsage());
        data.put("memory", server.getMemoryUsage());
        // data.put("requests", sessionService.getServerLoad(server.getId()));
        long activeLoad = userSessionRepository
        .countByServerIdAndStatus(server.getId(), SessionStatus.ACTIVE);

data.put("requests", activeLoad);
        data.put("response", 100.0); // You can calculate average response time per server
        return data;
    }
    
    /**
     * Check if ML service is available
     */
    public boolean isMLServiceHealthy() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(mlServiceUrl + "/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            return response != null && "healthy".equals(response.get("status"));
        } catch (Exception e) {
            return false;
        }
    }
    public Map<String, Object> getMLDecision(Long userId) {

    try {

        
        List<Server> servers = serverRepository.findAll(); 

        Map<String, Object> userStats = getUserStats(userId);

        List<Map<String, Object>> serverData = servers.stream()
                .map(this::convertServerToMLFormat)
                .collect(Collectors.toList());

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("user", userStats);
        requestPayload.put("servers", serverData);

        return webClient.post()
                .uri(mlServiceUrl + "/predict-server")
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

    } catch (Exception e) {

        System.out.println("ML service error: " + e.getMessage());
        return new HashMap<>();
    }
}
}