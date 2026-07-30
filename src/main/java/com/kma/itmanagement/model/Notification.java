package com.kma.itmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientUsername; // Target user (or 'ROLE_ADMIN' for system-wide admin alerts)
    private String title;
    private String message;
    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public Notification(String recipientUsername, String title, String message) {
        this.recipientUsername = recipientUsername;
        this.title = title;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}