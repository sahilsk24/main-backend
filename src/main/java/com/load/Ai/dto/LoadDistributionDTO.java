package com.load.Ai.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadDistributionDTO {
    private String serverName;
    private Integer requestsHandled;
    private Double loadPercentage;
}