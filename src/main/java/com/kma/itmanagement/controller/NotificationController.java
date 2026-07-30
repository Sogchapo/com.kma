package com.kma.itmanagement.controller;

import com.kma.itmanagement.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications/read/{id}")
    public String markNotificationAsRead(@PathVariable("id") Long id, HttpServletRequest request) {
        notificationService.markAsRead(id);
        
        // Redirect back to the page the user was on
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}