package com.load.Ai.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.load.Ai.entity.Server;
import com.load.Ai.entity.ServerStatus;
import com.load.Ai.repository.ServerRepository;
import com.load.Ai.repository.UserSessionRepository;
import com.load.Ai.repository.UserSessionRepository;
import com.load.Ai.entity.SessionStatus;

@Service
public class LoadBalancerService {

    @Autowired
    private ServerRepository serverRepository;

    public Server selectBestServer() {
        List<Server> activeServers = serverRepository.findByStatus(ServerStatus.ACTIVE);

        if (activeServers.isEmpty()) {
            throw new RuntimeException("No active servers available");
        }

        // Select server with lowest score
        Optional<Server> bestServer = activeServers.stream()
                .min((s1, s2) -> {
                    double score1 = calculateServerScore(s1);
                    double score2 = calculateServerScore(s2);
                    return Double.compare(score1, score2);
                });

        return bestServer.orElseThrow(() ->
                new RuntimeException("Failed to select server"));
    }
@Autowired
private UserSessionRepository userSessionRepository;

private double calculateServerScore(Server server) {

    double loadWeight   = 0.4;
    double cpuWeight    = 0.3;
    double memoryWeight = 0.3;

    // 🔥 Get REAL active session load
    long activeLoad =
            userSessionRepository.countByServerIdAndStatus(
                    server.getId(),
                    SessionStatus.ACTIVE
            );

    double normalizedLoad   = activeLoad / 20.0;
    double normalizedCpu    = server.getCpuUsage() / 100.0;
    double normalizedMemory = server.getMemoryUsage() / 100.0;

    return (loadWeight * normalizedLoad) +
           (cpuWeight * normalizedCpu) +
           (memoryWeight * normalizedMemory);
}


    @Transactional
public void updateServerLoad(Long serverId, boolean increment) {

    if (increment) {

        // 1️⃣ Increase current load safely
        serverRepository.incrementLoad(serverId);

        // 2️⃣ Fetch updated server
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        // 3️⃣ Increase total handled count
        server.setTotalRequestsHandled(
                server.getTotalRequestsHandled() + 1
        );

        updateServerStatus(server);

        serverRepository.save(server);

    } else {

        // Decrease current load safely
        serverRepository.decrementLoad(serverId);

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        updateServerStatus(server);

        serverRepository.save(server);
    }
}




    private void updateServerStatus(Server server) {
        if (server.getCurrentLoad() > 10 ||
            server.getCpuUsage() > 80 ||
            server.getMemoryUsage() > 85) {

            server.setStatus(ServerStatus.OVERLOADED);
            System.out.println("[STATUS] " + server.getServerName() +
                    " → OVERLOADED");
        } else {
            server.setStatus(ServerStatus.ACTIVE);
        }
    }
}

