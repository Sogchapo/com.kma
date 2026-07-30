package com.kma.itmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String priority; // Low, Medium, High, Critical

    @Column(nullable = false)
    private String status; // Open, In Progress, Resolved

    @Column(nullable = false)
    private String submittedBy; // KMA Employee name/department

    private LocalDateTime createdAt;

    // --- Constructors ---
    public Ticket() {
        this.createdAt = LocalDateTime.now();
    }

    public Ticket(String title, String description, String priority, String status, String submittedBy) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.submittedBy = submittedBy;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}