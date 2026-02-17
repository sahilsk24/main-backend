package com.load.Ai.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private UserStatsDTO userStats;
    private List<ServerHealthDTO> serverHealthList;
    private List<LoadDistributionDTO> loadDistribution;
    private SystemOverviewDTO systemOverview;
}
