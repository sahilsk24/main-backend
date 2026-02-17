package com.load.Ai.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardDTO {
    private UserUsageStatsDTO usageStats;
    private List<RequestHistoryDTO> requestHistory;
    private CurrentSessionDTO currentSession;
}
