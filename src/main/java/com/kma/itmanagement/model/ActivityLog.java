package com.kma.itmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    
    private String module;  // e.g., "ASSETS", "USER_MGMT", "REQUESTS", "HELPDESK", "AUTH"
    
    private String action;  // e.g., "CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT"

    @Column(length = 1000)
    private String details; // e.g., "Registered user Nana Okyere" or "Updated asset KMA-PC-001"

    @Column(name = "ip_address")
    private String ipAddress;

    private LocalDateTime timestamp;

    public ActivityLog() {}

    // Main constructor for detailed activity logging
    public ActivityLog(String username, String module, String action, String details, String ipAddress, LocalDateTime timestamp) {
        this.username = username;
        this.module = module;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    // Constructor for backward compatibility (basic login/logout events)
    public ActivityLog(String username, String action, String ipAddress, LocalDateTime timestamp) {
        this(username, "AUTH", action, "User performed " + action, ipAddress, timestamp);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}