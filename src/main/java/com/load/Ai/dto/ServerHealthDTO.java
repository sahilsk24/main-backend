package com.load.Ai.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerHealthDTO {

    private Long serverId;
    private String serverName;
    private String serverIp;
    private String status;
    private Double cpuUsage;
    private Double memoryUsage;
    private Long currentLoad;              // 🔥 change to Long
    private Integer totalRequestsHandled;
}
