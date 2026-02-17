package com.load.Ai.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemOverviewDTO {
    private Integer totalServers;
    private Integer activeServers;
    private Integer overloadedServers;
    private Integer inactiveServers;
    private Long totalRequestsToday;
    private Double averageResponseTime;
}