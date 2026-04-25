// package com.load.Ai.service;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import com.load.Ai.entity.Server;
// import com.load.Ai.entity.ServerStatus;
// import com.load.Ai.repository.ServerRepository;

// @Service
// public class ServerMonitoringService {

//     @Autowired
//     private ServerRepository serverRepository;

//     @Autowired
//     private ServerMetricsService serverMetricsService;

//     @Scheduled(fixedRate = 5000)
// public void updateAllServerMetrics() {

//     List<Server> servers = serverRepository.findAll();

//     for (Server server : servers) {

//         try {

//             double cpu;
//             double memory;

//             // REAL metric servers
//             if ("REAL".equalsIgnoreCase(server.getMetricsMode())) {

//                 cpu = serverMetricsService.getCpuUsage(
//                         server.getServerIp(),
//                         server.getServerPort());

//                 memory = serverMetricsService.getMemoryUsage(
//                         server.getServerIp(),
//                         server.getServerPort());
//             }
//             // MANUAL demo server (overloaded simulation)
//             else {

//                 cpu = 95.0;   // Always overloaded
//                 memory = 90.0;
//             }

//             server.setCpuUsage(cpu);
//             server.setMemoryUsage(memory);

//             if (cpu > 80 || memory > 85 || server.getCurrentLoad() > 10) {
//                 server.setStatus(ServerStatus.OVERLOADED);
//             } else {
//                 server.setStatus(ServerStatus.ACTIVE);
//             }

//             serverRepository.save(server);

//         } catch (Exception e) {

//     System.out.println("Server unreachable: " + server.getServerName());
//     e.printStackTrace();   // ADD THIS

//     server.setStatus(ServerStatus.INACTIVE);
//     serverRepository.save(server);
// }
//     }
// }
// }


package com.load.Ai.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Scheduled(fixedRate = 5000)
    public void updateAllServerMetrics() {

        List<Server> servers = serverRepository.findAll();

        for (Server server : servers) {

            try {

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

                    cpu = 0.21;
                    memory = 1.9;
                }

                server.setCpuUsage(cpu);
                server.setMemoryUsage(memory);

                if (cpu > 80 || memory > 85 || server.getCurrentLoad() > 10) {
                    server.setStatus(ServerStatus.OVERLOADED);
                } else {
                    server.setStatus(ServerStatus.ACTIVE);
                }

                serverRepository.save(server);

                System.out.println("Updated server: " + server.getServerName() +
                        " CPU=" + cpu + " MEM=" + memory);

            } catch (Exception e) {

                System.out.println("Server unreachable: " + server.getServerName());
                e.printStackTrace();

                server.setStatus(ServerStatus.INACTIVE);
                serverRepository.save(server);
            }
        }
    }
}