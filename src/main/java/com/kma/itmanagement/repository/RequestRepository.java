package com.kma.itmanagement.repository;

import com.kma.itmanagement.model.Request;
import com.kma.itmanagement.model.Request.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // Retrieve requests submitted by a specific staff member
    List<Request> findByRequestedByOrderByRequestedAtDesc(String requestedBy);

    // Filter requests by status (e.g., all PENDING for admin review)
    List<Request> findByStatusOrderByRequestedAtDesc(RequestStatus status);

    // Count pending requests for KPI badges
    long countByStatus(RequestStatus status);
}