package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.Request;
import com.kma.itmanagement.service.NotificationService;
import com.kma.itmanagement.service.RequestService;
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

    public RequestController(RequestService requestService, NotificationService notificationService) {
        this.requestService = requestService;
        this.notificationService = notificationService;
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

        // Check if user is ADMIN to determine view scope
        // (Assuming standard Spring Security principal roles check)
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
                                RedirectAttributes redirectAttributes) {
        if (principal != null) {
            request.setRequestedBy(principal.getName());
            requestService.createRequest(request);

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
                                 RedirectAttributes redirectAttributes) {
        requestService.approveRequest(id, adminComments);
        
        redirectAttributes.addFlashAttribute("toastMessage", "Request #" + id + " approved!");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/requests";
    }

    // Admin Action: Reject
    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectRequest(@PathVariable("id") Long id,
                                @RequestParam("adminComments") String adminComments,
                                RedirectAttributes redirectAttributes) {
        requestService.rejectRequest(id, adminComments);

        redirectAttributes.addFlashAttribute("toastMessage", "Request #" + id + " rejected.");
        redirectAttributes.addFlashAttribute("toastType", "error");
        return "redirect:/requests";
    }
}