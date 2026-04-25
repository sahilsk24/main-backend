package com.load.Ai.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.load.Ai.service.SessionService;

@RestController
@RequestMapping("/api/admin")
public class AdminStatsController {

    @Autowired
    private SessionService sessionService;

    @GetMapping("/user-stats")
    public ResponseEntity<?> getUserStats() {

        long activeUsers = sessionService.getActiveUserCount();
        long inactiveUsers = sessionService.getInactiveUserCount();

        Map<String, Object> response = new HashMap<>();
        response.put("activeUsers", activeUsers);
        response.put("inactiveUsers", inactiveUsers);

        return ResponseEntity.ok(response);
    }
}