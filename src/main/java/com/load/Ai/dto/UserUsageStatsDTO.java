package com.load.Ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUsageStatsDTO {
    private Long totalRequests;
    private Long requestsToday;
    private Double averageResponseTime;
    private Long totalTimeSpent; // in seconds
    private String mostUsedServer;
}