package com.kma.itmanagement.service;

import com.kma.itmanagement.model.ActivityLog;
import com.kma.itmanagement.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Standard login/logout logging (backward compatibility)
     */
    public void log(String username, String action, String ipAddress) {
        logActivity(username, "AUTH", action, "User performed " + action, ipAddress);
    }

    /**
     * Detailed activity logging for system-wide operations
     */
    public void logActivity(String username, String module, String action, String details, String ipAddress) {
        ActivityLog log = new ActivityLog(
            username,
            module != null ? module.toUpperCase() : "SYSTEM",
            action != null ? action.toUpperCase() : "UNKNOWN",
            details,
            ipAddress != null ? ipAddress : "127.0.0.1",
            LocalDateTime.now()
        );
        repository.save(log);
    }

    public List<ActivityLog> getAllLogs() {
        return repository.findAllByOrderByTimestampDesc();
    }
}