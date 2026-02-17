package com.load.Ai.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.load.Ai.entity.Server;

@Service
public class ServerHealthService {

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, Object> fetchHealth(Server server) {

        String url = "http://" 
                   + server.getServerIp() 
                   + ":" 
                   + server.getServerPort() 
                   + "/api/health";

        return restTemplate.getForObject(url, Map.class);
    }
}
