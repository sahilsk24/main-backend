package com.load.Ai.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.load.Ai.dto.CurrentSessionDTO;
import com.load.Ai.dto.RequestHistoryDTO;
import com.load.Ai.dto.UserDashboardDTO;
import com.load.Ai.dto.UserUsageStatsDTO;
import com.load.Ai.security.UserDetailsImpl;
import com.load.Ai.service.UserDashboardService;

// @RestController
// @RequestMapping("/api/user")
// @CrossOrigin(origins = "*", maxAge = 3600)
// @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
// public class UserDashboardController {
    
//     @Autowired
//     private UserDashboardService userDashboardService;
    
//     /**
//      * Get complete user dashboard data
//      */
//     @GetMapping("/dashboard")
//     public ResponseEntity<UserDashboardDTO> getUserDashboard() {
//         try {
//             Long userId = getCurrentUserId();
//             UserDashboardDTO dashboard = userDashboardService.getUserDashboard(userId);
//             return ResponseEntity.ok(dashboard);
//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.badRequest().build();
//         }
//     }
    
//     /**
//      * Get user usage statistics
//      */
//     @GetMapping("/usage-stats")
//     public ResponseEntity<UserUsageStatsDTO> getUserUsageStats() {
//         try {
//             Long userId = getCurrentUserId();
//             UserUsageStatsDTO stats = userDashboardService.getUserUsageStats(userId);
//             return ResponseEntity.ok(stats);
//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.badRequest().build();
//         }
//     }
    
//     /**
//      * Get user request history
//      */
//     @GetMapping("/request-history")
//     public ResponseEntity<List<RequestHistoryDTO>> getRequestHistory(
//             @RequestParam(defaultValue = "20") int limit) {
//         try {
//             Long userId = getCurrentUserId();
//             List<RequestHistoryDTO> history = userDashboardService.getRequestHistory(userId, limit);
//             return ResponseEntity.ok(history);
//         } catch (Exception e) {
//             e.printStackTrace();
//             return ResponseEntity.badRequest().build();
//         }
//     }
    
//     /**
//      * Get current session information
//      */
//    @GetMapping("/dashboard")
// public ResponseEntity<UserDashboardDTO> getUserDashboard(Authentication authentication) {

//     UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//     Long userId = userDetails.getId();

//     UserDashboardDTO dashboard = userDashboardService.getUserDashboard(userId);
//     return ResponseEntity.ok(dashboard);
// }

    
    /**
     * Helper method to get current logged-in user ID
     */
    // private Long getCurrentUserId() {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    //     return userDetails.getId();
    // }
// }

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UserDashboardController {

    @Autowired
    private UserDashboardService userDashboardService;

    /**
     * Get complete user dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<UserDashboardDTO> getUserDashboard(Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        UserDashboardDTO dashboard = userDashboardService.getUserDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get user usage statistics
     */
    @GetMapping("/usage-stats")
    public ResponseEntity<UserUsageStatsDTO> getUserUsageStats(Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        UserUsageStatsDTO stats = userDashboardService.getUserUsageStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get user request history
     */
    @GetMapping("/request-history")
    public ResponseEntity<List<RequestHistoryDTO>> getRequestHistory(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        List<RequestHistoryDTO> history =
                userDashboardService.getRequestHistory(userId, limit);

        return ResponseEntity.ok(history);
    }

    /**
     * Get current session information
     */
    @GetMapping("/current-session")
    public ResponseEntity<CurrentSessionDTO> getCurrentSession(Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        CurrentSessionDTO session =
                userDashboardService.getCurrentSession(userId);

        return ResponseEntity.ok(session);
    }
}
