package com.load.Ai.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountingReportDTO {
    private Long userId;
    private String username;
    private String email;
    private Long totalRequests;
    private Double averageResponseTime;
    private Long totalTimeSpent; // in seconds
    private Integer loginCount;
}