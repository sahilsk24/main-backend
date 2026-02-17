package com.load.Ai.controller;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.load.Ai.dto.AuthResponse;
import com.load.Ai.dto.LoginRequest;
import com.load.Ai.dto.MessageResponse;
import com.load.Ai.dto.SignupRequest;
import com.load.Ai.entity.LoginHistory;
import com.load.Ai.entity.LoginStatus;
import com.load.Ai.entity.Role;
import com.load.Ai.entity.User;
import com.load.Ai.repository.LoginHistoryRepository;
import com.load.Ai.security.JwtUtils;
import com.load.Ai.security.UserDetailsImpl;
import com.load.Ai.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    

    @Autowired
     private LoginHistoryRepository loginHistoryRepository;


    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            User user = userService.registerUser(signupRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
   @PostMapping("/login")
public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, 
                                          HttpServletRequest request) {
    try {
        // Find user by email or username
        User user = userService.findByEmailOrUsername(loginRequest.getEmailOrUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Authenticate using username (Spring Security uses username field)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        // Get role without ROLE_ prefix
        String role = roles.isEmpty() ? "" : roles.get(0).replace("ROLE_", "");
        
        // Log login history
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setIpAddress(getClientIp(request));
        loginHistory.setUserAgent(request.getHeader("User-Agent"));
        loginHistory.setLoginTime(LocalDateTime.now());
        loginHistory.setStatus(LoginStatus.SUCCESS);
        loginHistoryRepository.save(loginHistory);
        
        return ResponseEntity.ok(new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role
        ));
    } catch (Exception e) {
        e.printStackTrace();
        
        // Log failed login attempt
        try {
            User user = userService.findByEmailOrUsername(loginRequest.getEmailOrUsername()).orElse(null);
            if (user != null) {
                LoginHistory loginHistory = new LoginHistory();
                loginHistory.setUser(user);
                loginHistory.setIpAddress(getClientIp(request));
                loginHistory.setUserAgent(request.getHeader("User-Agent"));
                loginHistory.setLoginTime(LocalDateTime.now());
                loginHistory.setStatus(LoginStatus.FAILED);
                loginHistoryRepository.save(loginHistory);
            }
        } catch (Exception ex) {
            // Ignore if we can't log the failed attempt
        }
        
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid credentials"));
    }
}

    
   @PostMapping("/admin/login")
public ResponseEntity<?> authenticateAdmin(@Valid @RequestBody LoginRequest loginRequest,
                                          HttpServletRequest request) {
    try {
        // Find user by email or username
        User user = userService.findByEmailOrUsername(loginRequest.getEmailOrUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has ADMIN role
        if (user.getRole() != Role.ADMIN) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Access denied. Admin privileges required."));
        }
        
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        String role = roles.isEmpty() ? "" : roles.get(0).replace("ROLE_", "");
        
        // Log login history
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setIpAddress(getClientIp(request));
        loginHistory.setUserAgent(request.getHeader("User-Agent"));
        loginHistory.setLoginTime(LocalDateTime.now());
        loginHistory.setStatus(LoginStatus.SUCCESS);
        loginHistoryRepository.save(loginHistory);
        
        return ResponseEntity.ok(new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role
        ));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid credentials"));
    }
}

private String getClientIp(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null) {
        return request.getRemoteAddr();
    }
    return xfHeader.split(",")[0];
}
}