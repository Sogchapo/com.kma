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
    private String action; // e.g., "LOGIN", "LOGOUT"
    
    @Column(name = "ip_address")
    private String ipAddress;

    private LocalDateTime timestamp;

    public ActivityLog() {}

    public ActivityLog(String username, String action, String ipAddress, LocalDateTime timestamp) {
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}