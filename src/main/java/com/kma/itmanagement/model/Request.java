package com.kma.itmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requestedBy; // Username of the requesting staff member

    @Column(nullable = false)
    private String itemRequested; // e.g., "Dell Latitude 5420", "HDMI Cable"

    private String department;

    @Column(length = 500)
    private String reason; // Justification for the request

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    private String adminComments; // Feedback or approval notes from Admin

    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime processedAt;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }

    // Default Constructor
    public Request() {}

    // Convenience Constructor
    public Request(String requestedBy, String itemRequested, String department, String reason) {
        this.requestedBy = requestedBy;
        this.itemRequested = itemRequested;
        this.department = department;
        this.reason = reason;
        this.requestedAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getItemRequested() { return itemRequested; }
    public void setItemRequested(String itemRequested) { this.itemRequested = itemRequested; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getAdminComments() { return adminComments; }
    public void setAdminComments(String adminComments) { this.adminComments = adminComments; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}