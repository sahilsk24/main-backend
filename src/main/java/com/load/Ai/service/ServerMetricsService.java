package com.load.Ai.service;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServerMetricsService {






        
    private final RestTemplate restTemplate = new RestTemplate();

    // ================= CPU =================
    public double getCpuUsage(String ip, int port) {

        String url = "http://" + ip + ":" + port +
                     "/actuator/metrics/process.cpu.usage";

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        if (response == null) return 0.0;

        List<Map<String, Object>> measurements =
                (List<Map<String, Object>>) response.get("measurements");

        double cpuFraction =
                ((Number) measurements.get(0).get("value")).doubleValue();

        return cpuFraction * 100; // convert to %
    }

    // ================= MEMORY =================
    public double getMemoryUsage(String ip, int port) {

        String usedUrl = "http://" + ip + ":" + port +
                "/actuator/metrics/jvm.memory.used?tag=area:heap";

        String maxUrl = "http://" + ip + ":" + port +
                "/actuator/metrics/jvm.memory.max?tag=area:heap";

        Map<String, Object> usedRes =
                restTemplate.getForObject(usedUrl, Map.class);

        Map<String, Object> maxRes =
                restTemplate.getForObject(maxUrl, Map.class);

        if (usedRes == null || maxRes == null) return 0.0;

        double used = extractValue(usedRes);
        double max  = extractValue(maxRes);

        if (max == 0) return 0.0;

        return (used / max) * 100;
    }

    private double extractValue(Map<String, Object> response) {

        List<Map<String, Object>> measurements =
                (List<Map<String, Object>>) response.get("measurements");

        return ((Number) measurements.get(0).get("value")).doubleValue();
    }
}
