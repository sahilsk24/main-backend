package com.load.Ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.load.Ai.dto.MessageResponse;
import com.load.Ai.entity.Server;
import com.load.Ai.entity.ServerStatus;
import com.load.Ai.repository.ServerRepository;
import com.load.Ai.service.ServerMetricsService;
import com.load.Ai.service.SessionService;

@RestController
@RequestMapping("/api/admin/servers")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class ServerManagementController {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServerMetricsService serverMetricsService;
   @Autowired
private SessionService sessionService;
    /**
     * Get all servers
     */
    @GetMapping
    public ResponseEntity<List<Server>> getAllServers() {
        return ResponseEntity.ok(serverRepository.findAll());
    }

    /**
     * Get server by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getServerById(@PathVariable Long id) {
        try {
            Server server = serverRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Server not found"));
            return ResponseEntity.ok(server);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Update server status manually
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateServerStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusData) {

        try {
            Server server = serverRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Server not found"));

            ServerStatus newStatus =
                    ServerStatus.valueOf(statusData.get("status"));

            server.setStatus(newStatus);
            serverRepository.save(server);

            return ResponseEntity.ok(
                    new MessageResponse("Server status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Refresh REAL server metrics (CPU & Memory from Actuator)
     */
    @PostMapping("/{id}/simulate-metrics")
    public ResponseEntity<?> refreshMetrics(@PathVariable Long id) {
        try {
            Server server = serverRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Server not found"));


//              double cpu;
// double memory;

// // ===== SERVER-1 → REAL METRICS =====
// if (server.getId() == 1) {

//     cpu = serverMetricsService.getCpuUsage(
//             server.getServerIp(),
//             server.getServerPort()
//     );

//     memory = serverMetricsService.getMemoryUsage(
//             server.getServerIp(),
//             server.getServerPort()
//     );
// }
// // ===== SERVER-2 → MANUAL VALUES =====
// else if (server.getId() == 2) {
//   cpu = serverMetricsService.getCpuUsage(
//             server.getServerIp(),
//             server.getServerPort()
//     );

//     memory = serverMetricsService.getMemoryUsage(
//             server.getServerIp(),
//             server.getServerPort()
//     );
// }
// // ===== SERVER-3 → MANUAL VALUES =====
// else {

//     cpu = 88.0;      // 🟢 best server
//     memory = 82.0;
// }

double cpu;
double memory;

if ("REAL".equalsIgnoreCase(server.getMetricsMode())) {

    cpu = serverMetricsService.getCpuUsage(
            server.getServerIp(),
            server.getServerPort());

    memory = serverMetricsService.getMemoryUsage(
            server.getServerIp(),
            server.getServerPort());

} else {

    cpu = 95.0;   // demo overload
    memory = 90.0;
}



            server.setCpuUsage(cpu);
            server.setMemoryUsage(memory);

            // ===== Derive status =====
           long dynamicLoad = sessionService.getServerLoad(server.getId());

            if (cpu > 80 || memory > 85 || dynamicLoad > 10){
                server.setStatus(ServerStatus.OVERLOADED);
            } else {
                server.setStatus(ServerStatus.ACTIVE);
            }

            serverRepository.save(server);
            return ResponseEntity.ok(server);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Reset server metrics
     */
    @PostMapping("/{id}/reset")
    public ResponseEntity<?> resetServer(@PathVariable Long id) {
        try {
            Server server = serverRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Server not found"));

            server.setCpuUsage(0.0);
            server.setMemoryUsage(0.0);
            server.setCurrentLoad(0);
            server.setStatus(ServerStatus.ACTIVE);

            serverRepository.save(server);
            return ResponseEntity.ok(
                    new MessageResponse("Server reset successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
