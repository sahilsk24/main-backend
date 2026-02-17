package com.load.Ai.dto;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestHistoryDTO {
    private Long requestId;
    private String requestType;
    private String endpoint;
    private String serverName;
    private Integer responseTime;
    private Integer statusCode;
    private LocalDateTime requestTime;
}