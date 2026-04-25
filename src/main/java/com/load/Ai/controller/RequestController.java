package com.load.Ai.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.load.Ai.dto.MessageResponse;
import com.load.Ai.entity.RequestLog;
import com.load.Ai.entity.Server;
import com.load.Ai.entity.SessionStatus;
import com.load.Ai.entity.User;
import com.load.Ai.entity.UserSession;
import com.load.Ai.repository.RequestLogRepository;
import com.load.Ai.repository.ServerRepository;
import com.load.Ai.repository.UserRepository;
import com.load.Ai.repository.UserSessionRepository;
import com.load.Ai.security.UserDetailsImpl;
import com.load.Ai.service.SessionService;

@RestController
@RequestMapping("/api/request")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class RequestController {

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Autowired
    private UserRepository userRepository;
    
   @Autowired
private ServerRepository serverRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SessionService sessionService;

    private final Random random = new Random();
   

    @PostMapping("/send")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, String> requestData) {

        Long userId = getCurrentUserId();

        // ✅ Refresh session activity
        sessionService.refreshUserActivity(userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Get assigned server from session
            UserSession session = userSessionRepository
                    .findByUserIdAndStatus(userId, SessionStatus.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("No active session"));

            Server selectedServer = session.getServer();

            System.out.println("Using Assigned Server: " + selectedServer.getServerName());
            // System.out.println("Current Load: " + selectedServer.getCurrentLoad());
            long dynamicLoad = sessionService.getServerLoad(selectedServer.getId());

           System.out.println("Current Load: " + dynamicLoad);

           


            long startTime = System.currentTimeMillis();

            int processingTime = 50 + random.nextInt(450);
            Thread.sleep(processingTime);

            long actualResponseTime = System.currentTimeMillis() - startTime;

            RequestLog log = new RequestLog();
            log.setUser(user);
            log.setServer(selectedServer);
            log.setRequestType(requestData.getOrDefault("type", "POST"));
            log.setEndpoint(requestData.getOrDefault("endpoint", "/api/data"));
            log.setResponseTime((int) actualResponseTime);
            log.setStatusCode(200);
            log.setRequestTime(LocalDateTime.now());
            requestLogRepository.save(log);

            // ✅ Increase total handled count
selectedServer.setTotalRequestsHandled(
        selectedServer.getTotalRequestsHandled() + 1
);
serverRepository.save(selectedServer);
          String selectionMethod = session.getSelectionMethod();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request processed successfully");
            response.put("assignedServer", selectedServer.getServerName());
            // response.put("currentLoad", selectedServer.getCurrentLoad());
             response.put("currentLoad", dynamicLoad);
            response.put("responseTime", actualResponseTime + "ms");
            response.put("selectionMethod", selectionMethod);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }



}