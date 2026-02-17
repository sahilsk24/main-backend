package com.load.Ai.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.load.Ai.entity.Server;
import com.load.Ai.entity.ServerStatus;
import com.load.Ai.repository.ServerRepository;

@Service
public class LoadBalancerService {
    
    @Autowired
    private ServerRepository serverRepository;
    
    /**
     * AI-based load balancing algorithm
     * Selects the best server based on:
     * 1. Server status (must be ACTIVE)
     * 2. Current load
     * 3. CPU usage
     * 4. Memory usage
     */
    public Server selectBestServer() {
        List<Server> activeServers = serverRepository.findByStatus(ServerStatus.ACTIVE);
        
        if (activeServers.isEmpty()) {
            throw new RuntimeException("No active servers available");
        }
        
        // Calculate score for each server (lower is better)
        Optional<Server> bestServer = activeServers.stream()
                .min((s1, s2) -> {
                    double score1 = calculateServerScore(s1);
                    double score2 = calculateServerScore(s2);
                    return Double.compare(score1, score2);
                });
        
        return bestServer.orElseThrow(() -> new RuntimeException("Failed to select server"));
    }
    
    /**
     * Calculate server score based on multiple factors
     * Lower score = better server
     */
    private double calculateServerScore(Server server) {
        double loadWeight = 0.4;
        double cpuWeight = 0.3;
        double memoryWeight = 0.3;
        
        double normalizedLoad = server.getCurrentLoad() / 10.0; // Assuming max load of 10
        double normalizedCpu = server.getCpuUsage() / 100.0;
        double normalizedMemory = server.getMemoryUsage() / 100.0;
        
        return (loadWeight * normalizedLoad) + 
               (cpuWeight * normalizedCpu) + 
               (memoryWeight * normalizedMemory);
    }
    
    /**
     * Update server metrics after handling a request
     */
    public void updateServerLoad(Long serverId, boolean increment) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));
        
        if (increment) {
            server.setCurrentLoad(server.getCurrentLoad() + 1);
            server.setTotalRequestsHandled(server.getTotalRequestsHandled() + 1);
        } else {
            server.setCurrentLoad(Math.max(0, server.getCurrentLoad() - 1));
        }
        
        // Update server status based on load
        if (server.getCurrentLoad() > 10 || server.getCpuUsage() > 80 || server.getMemoryUsage() > 85) {
            server.setStatus(ServerStatus.OVERLOADED);
        } else {
            server.setStatus(ServerStatus.ACTIVE);
        }
        
        serverRepository.save(server);
    }
}
