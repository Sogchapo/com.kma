package com.kma.itmanagement.service;

import com.kma.itmanagement.model.Request;
import com.kma.itmanagement.model.Request.RequestStatus;
import com.kma.itmanagement.repository.RequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final NotificationService notificationService;

    public RequestService(RequestRepository requestRepository, NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
    }

    // Submit a new staff request
    public Request createRequest(Request request) {
        request.setStatus(RequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    // Get all requests (for Admin overview)
    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }

    // Get requests by a specific staff member
    public List<Request> getRequestsByUser(String username) {
        return requestRepository.findByRequestedByOrderByRequestedAtDesc(username);
    }

    // Fetch single request by ID
    public Request getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Request ID: " + id));
    }

    // Admin Action: Approve Request
    public Request approveRequest(Long requestId, String adminComments) {
        Request request = getRequestById(requestId);
        request.setStatus(RequestStatus.APPROVED);
        request.setAdminComments(adminComments);
        request.setProcessedAt(LocalDateTime.now());
        
        Request updatedRequest = requestRepository.save(request);

        // Notify staff member via Notification System
        notificationService.sendNotification(
            request.getRequestedBy(),
            "Request Approved ✅",
            "Your request for '" + request.getItemRequested() + "' has been APPROVED."
        );

        return updatedRequest;
    }

    // Admin Action: Reject Request
    public Request rejectRequest(Long requestId, String adminComments) {
        Request request = getRequestById(requestId);
        request.setStatus(RequestStatus.REJECTED);
        request.setAdminComments(adminComments);
        request.setProcessedAt(LocalDateTime.now());

        Request updatedRequest = requestRepository.save(request);

        // Notify staff member via Notification System
        notificationService.sendNotification(
            request.getRequestedBy(),
            "Request Rejected 🚨",
            "Your request for '" + request.getItemRequested() + "' was rejected. Reason: " + adminComments
        );

        return updatedRequest;
    }

    // Count total pending requests for badges/cards
    public long getPendingRequestCount() {
        return requestRepository.countByStatus(RequestStatus.PENDING);
    }
}