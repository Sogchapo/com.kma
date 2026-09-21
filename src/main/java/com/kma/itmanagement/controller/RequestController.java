package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.Request;
import com.kma.itmanagement.service.ActivityLogService;
import com.kma.itmanagement.service.NotificationService;
import com.kma.itmanagement.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public RequestController(RequestService requestService, 
                             NotificationService notificationService,
                             ActivityLogService activityLogService) {
        this.requestService = requestService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    // View Request Workflow Page
    @GetMapping
    public String showRequestsPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        
        // Pass empty request object for modal form binding
        model.addAttribute("newRequest", new Request());

        List<Request> requests = requestService.getAllRequests(); // Admin sees all
        
        model.addAttribute("requests", requests);
        model.addAttribute("pendingCount", requestService.getPendingRequestCount());

        // Top nav notifications
        model.addAttribute("notifications", notificationService.getUserNotifications(username));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(username));

        return "requests"; // Renders templates/requests.html
    }

    // Submit new staff request
    @PostMapping("/new")
    public String submitRequest(@ModelAttribute("newRequest") Request request,
                                Principal principal,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {
        if (principal != null) {
            String username = principal.getName();
            request.setRequestedBy(username);
            requestService.createRequest(request);

            // Log activity: User submitted new request (using itemRequested)
            activityLogService.logActivity(
                username,
                "REQUESTS",
                "CREATE_REQUEST",
                "Submitted request for item: " + (request.getItemRequested() != null ? request.getItemRequested() : "Asset"),
                httpRequest.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("toastMessage", "Request submitted successfully!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        }
        return "redirect:/requests";
    }

    // Admin Action: Approve
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveRequest(@PathVariable("id") Long id,
                                 @RequestParam(value = "adminComments", defaultValue = "Approved") String adminComments,
                                 Principal principal,
                                 HttpServletRequest httpRequest,
                                 RedirectAttributes redirectAttributes) {
        requestService.approveRequest(id, adminComments);
        
        // Log activity: Admin approved request
        if (principal != null) {
            activityLogService.logActivity(
                principal.getName(),
                "REQUESTS",
                "APPROVE_REQUEST",
                "Approved Request #" + id + " with comment: " + adminComments,
                httpRequest.getRemoteAddr()
            );
        }

        redirectAttributes.addFlashAttribute("toastMessage", "Request #" + id + " approved!");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/requests";
    }

    // Admin Action: Reject
    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectRequest(@PathVariable("id") Long id,
                                @RequestParam("adminComments") String adminComments,
                                Principal principal,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {
        requestService.rejectRequest(id, adminComments);

        // Log activity: Admin rejected request
        if (principal != null) {
            activityLogService.logActivity(
                principal.getName(),
                "REQUESTS",
                "REJECT_REQUEST",
                "Rejected Request #" + id + " with comment: " + adminComments,
                httpRequest.getRemoteAddr()
            );
        }

        redirectAttributes.addFlashAttribute("toastMessage", "Request #" + id + " rejected.");
        redirectAttributes.addFlashAttribute("toastType", "error");
        return "redirect:/requests";
    }
}