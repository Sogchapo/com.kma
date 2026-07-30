package com.kma.itmanagement.controller;

import com.kma.itmanagement.service.ActivityLogService;
import com.kma.itmanagement.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public ActivityLogController(ActivityLogService activityLogService, NotificationService notificationService) {
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public String getActivityLogsPage(Model model, Principal principal) {
        model.addAttribute("logs", activityLogService.getAllLogs());

        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "activity-logs";
    }
}