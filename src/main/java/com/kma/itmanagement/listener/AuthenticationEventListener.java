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

        activityLogService.log(username, "LOGIN", ipAddress);
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ipAddress = "127.0.0.1";

        if (event.getAuthentication().getDetails() instanceof WebAuthenticationDetails details) {
            ipAddress = details.getRemoteAddress();
        }

        activityLogService.log(username, "LOGOUT", ipAddress);
    }
}