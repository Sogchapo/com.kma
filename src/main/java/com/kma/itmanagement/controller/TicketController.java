package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.Ticket;
import com.kma.itmanagement.model.TicketComment;
import com.kma.itmanagement.service.ActivityLogService;
import com.kma.itmanagement.service.NotificationService;
import com.kma.itmanagement.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class TicketController {

    private final TicketService ticketService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public TicketController(TicketService ticketService, 
                             NotificationService notificationService,
                             ActivityLogService activityLogService) {
        this.ticketService = ticketService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    // Displays the main Ticket / Helpdesk list
    @GetMapping("/tickets")
    public String getTicketsPage(Model model, Principal principal, HttpServletRequest request) {
        long totalTickets = ticketService.getTicketCount();
        long openTickets = ticketService.getOpenTicketCount();
        List<Ticket> tickets = ticketService.getAllTickets();

        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("tickets", tickets);

        // Attach notification context for the currently logged-in user & log access
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));

            activityLogService.logActivity(
                username,
                "HELPDESK",
                "VIEW",
                "Accessed Helpdesk tickets dashboard",
                request.getRemoteAddr()
            );
        }

        return "tickets"; // Renders templates/tickets.html
    }

    // Displays the "File a Support Ticket" Form
    @GetMapping("/tickets/new")
    public String showNewTicketForm(Model model, Principal principal) {
        Ticket ticket = new Ticket();
        // Pre-populate submitter name if principal is available
        if (principal != null) {
            ticket.setSubmittedBy(principal.getName());
        }
        model.addAttribute("ticket", ticket);

        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "new-ticket"; // Renders templates/new-ticket.html
    }

    // Saves a new ticket with an optional photo attachment to the database & notifies submitter/admin
    @PostMapping("/tickets/new")
    public String saveTicket(@ModelAttribute("ticket") Ticket ticket, 
                             @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                             Principal principal, 
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        ticket.setStatus("Open"); // Default starting status
        
        String authUsername = (principal != null) ? principal.getName() : "Anonymous";
        
        // If submitter wasn't explicitly filled in via the form model, fallback to authenticated user
        if (ticket.getSubmittedBy() == null || ticket.getSubmittedBy().trim().isEmpty()) {
            ticket.setSubmittedBy(authUsername);
        }

        // Handle Photo Attachment Upload (or direct device camera capture)
        if (attachment != null && !attachment.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/tickets/";
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalName = attachment.getOriginalFilename();
                String extension = ".png"; // Default fallback
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }

                String fileName = UUID.randomUUID().toString() + extension;
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, attachment.getBytes());

                ticket.setAttachmentImage(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        Ticket savedTicket = ticketService.saveTicket(ticket);

        // 1. Notify the user who created the ticket (if recognized)
        if (principal != null) {
            notificationService.sendNotification(
                authUsername,
                "Ticket Created",
                "Your support ticket #" + savedTicket.getId() + " (" + savedTicket.getTitle() + ") was logged."
            );
        }

        // 2. Send notification to admin account
        notificationService.sendNotification(
            "admin",
            "New Ticket Submitted",
            "Ticket #" + savedTicket.getId() + " (" + savedTicket.getTitle() + ") logged by " + ticket.getSubmittedBy() + "."
        );

        // 3. Log activity: Ticket Created
        activityLogService.logActivity(
            authUsername,
            "HELPDESK",
            "CREATE_TICKET",
            "Created support ticket #" + savedTicket.getId() + ": " + savedTicket.getTitle() + (ticket.getAttachmentImage() != null ? " (with photo attachment)" : ""),
            request.getRemoteAddr()
        );

        // Trigger Floating Toast Banner on Helpdesk
        redirectAttributes.addFlashAttribute("toastMessage", "Support ticket #" + savedTicket.getId() + " created successfully!");
        redirectAttributes.addFlashAttribute("toastType", "success");

        return "redirect:/tickets";
    }

    // Displays the detail view for a specific ticket including its comment thread
    @GetMapping("/tickets/detail/{id}")
    public String getTicketDetail(@PathVariable("id") Long id, Model model, Principal principal) {
        Ticket ticket = ticketService.getTicketById(id);
        List<TicketComment> comments = ticketService.getCommentsByTicketId(id);

        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", comments);

        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "ticket-detail"; // Renders templates/ticket-detail.html
    }

    // Processes new ticket comment entries
    @PostMapping("/tickets/detail/{id}/comment")
    public String addComment(@PathVariable("id") Long id, 
                             @RequestParam("content") String content, 
                             Principal principal, 
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        if (content != null && !content.trim().isEmpty()) {
            String author = (principal != null) ? principal.getName() : "Anonymous";
            ticketService.addComment(id, content.trim(), author);

            // Notify ticket owner if someone else commented, otherwise notify admin
            Ticket ticket = ticketService.getTicketById(id);
            if (ticket.getSubmittedBy() != null && !ticket.getSubmittedBy().equalsIgnoreCase(author)) {
                notificationService.sendNotification(
                    ticket.getSubmittedBy(),
                    "New Comment on Ticket #" + id,
                    author + " commented on your ticket."
                );
            } else {
                notificationService.sendNotification(
                    "admin",
                    "New Comment on Ticket #" + id,
                    author + " posted an update."
                );
            }

            // Log activity: Comment Added
            activityLogService.logActivity(
                author,
                "HELPDESK",
                "ADD_COMMENT",
                "Added comment to ticket #" + id,
                request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("toastMessage", "Comment posted successfully!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        }

        return "redirect:/tickets/detail/" + id;
    }

    // Processes status update and alerts the ticket creator
    @PostMapping("/tickets/detail/{id}/update-status")
    public String updateTicketStatus(@PathVariable("id") Long id, 
                                     @RequestParam("status") String status, 
                                     Principal principal,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        ticketService.updateTicketStatus(id, status);

        // Fetch ticket details to send targeted notification to the creator
        Ticket ticket = ticketService.getTicketById(id);
        if (ticket != null && ticket.getSubmittedBy() != null) {
            notificationService.sendNotification(
                ticket.getSubmittedBy(),
                "Ticket Status Updated",
                "Your ticket #" + id + " has been updated to status: " + status
            );
        }

        // Log activity: Status Updated
        if (principal != null) {
            activityLogService.logActivity(
                principal.getName(),
                "HELPDESK",
                "UPDATE_STATUS",
                "Updated ticket #" + id + " status to '" + status + "'",
                request.getRemoteAddr()
            );
        }

        // Trigger Floating Toast Banner on Ticket Detail
        redirectAttributes.addFlashAttribute("toastMessage", "Ticket #" + id + " status updated to '" + status + "'");
        redirectAttributes.addFlashAttribute("toastType", "success");

        return "redirect:/tickets/detail/" + id;
    }

    // Handles deleting a support ticket entry
    @GetMapping("/tickets/delete/{id}")
    public String deleteTicket(@PathVariable("id") Long id, 
                               Principal principal, 
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        ticketService.deleteTicketById(id);

        if (principal != null) {
            String username = principal.getName();
            notificationService.sendNotification(
                username,
                "Ticket Deleted",
                "Support ticket #" + id + " was permanently removed."
            );

            // Log activity: Ticket Deleted
            activityLogService.logActivity(
                username,
                "HELPDESK",
                "DELETE_TICKET",
                "Permanently deleted support ticket #" + id,
                request.getRemoteAddr()
            );
        }

        redirectAttributes.addFlashAttribute("toastMessage", "Ticket #" + id + " was permanently deleted.");
        redirectAttributes.addFlashAttribute("toastType", "warning");

        return "redirect:/tickets";
    }
}