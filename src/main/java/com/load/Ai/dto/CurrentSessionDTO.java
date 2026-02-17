package com.load.Ai.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSessionDTO {
    private LocalDateTime loginTime;
    private String ipAddress;
    private String status;
    private Long sessionDuration; // in seconds
}
