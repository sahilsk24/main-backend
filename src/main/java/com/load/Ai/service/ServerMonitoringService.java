package com.load.Ai.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.load.Ai.entity.Server;
import com.load.Ai.entity.ServerStatus;
import com.load.Ai.repository.ServerRepository;

@Service
public class ServerMonitoringService {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServerMetricsService serverMetricsService;

     @Value("${server.metrics.mode}")
    private String metricsMode;

    @Scheduled(fixedRate = 5000) // every 5 seconds
    public void updateAllServerMetrics() {

        List<Server> servers = serverRepository.findAll();

        for (Server server : servers) {
            try {

                // double cpu = serverMetricsService.getCpuUsage(
                //         server.getServerIp(),
                //         server.getServerPort()
                // );

                // double memory = serverMetricsService.getMemoryUsage(
                //         server.getServerIp(),
                //         server.getServerPort()
                // );

                  double cpu;
                double memory;

                if ("SIMULATION".equalsIgnoreCase(metricsMode)) {

                    // demo values
                    if (server.getId() == 2) {
                        cpu = 20.0;
                        memory = 25.0;
                    } else if (server.getId() == 3) {
                        cpu = 90.0;
                        memory = 88.0;
                    } else {
                        cpu = 50.0;
                        memory = 45.0;
                    }

                } else {

                    cpu = serverMetricsService.getCpuUsage(
                            server.getServerIp(),
                            server.getServerPort());

                    memory = serverMetricsService.getMemoryUsage(
                            server.getServerIp(),
                            server.getServerPort());
                }

                server.setCpuUsage(cpu);
                server.setMemoryUsage(memory);

                if (cpu > 80 || memory > 85 || server.getCurrentLoad() > 10) {
                    server.setStatus(ServerStatus.OVERLOADED);
                } else {
                    server.setStatus(ServerStatus.ACTIVE);
                }

                serverRepository.save(server);

            } catch (Exception e) {
                server.setStatus(ServerStatus.INACTIVE);
                serverRepository.save(server);
            }
        }
    }
}
