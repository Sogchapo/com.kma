package com.kma.itmanagement.listener;

import com.kma.itmanagement.service.ActivityLogService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    private final ActivityLogService activityLogService;

    public AuthenticationEventListener(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ipAddress = "127.0.0.1";

        if (event.getAuthentication().getDetails() instanceof WebAuthenticationDetails details) {
            ipAddress = details.getRemoteAddress();
        }

        // Log rich login activity under the AUTH module
        activityLogService.logActivity(
            username,
            "AUTH",
            "LOGIN",
            "User successfully authenticated and logged into the portal",
            ipAddress
        );
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        // Note: event.getAuthentication() can sometimes be null on logout depending on the handler
        if (event.getAuthentication() != null) {
            String username = event.getAuthentication().getName();
            String ipAddress = "127.0.0.1";

            if (event.getAuthentication().getDetails() instanceof WebAuthenticationDetails details) {
                ipAddress = details.getRemoteAddress();
            }

            // Log rich logout activity under the AUTH module
            activityLogService.logActivity(
                username,
                "AUTH",
                "LOGOUT",
                "User successfully terminated session and logged out",
                ipAddress
            );
        }
    }
}