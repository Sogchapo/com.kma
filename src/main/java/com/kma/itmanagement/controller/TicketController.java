package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.Ticket;
import com.kma.itmanagement.model.TicketComment;
import com.kma.itmanagement.service.NotificationService;
import com.kma.itmanagement.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class TicketController {

    private final TicketService ticketService;
    private final NotificationService notificationService;

    public TicketController(TicketService ticketService, NotificationService notificationService) {
        this.ticketService = ticketService;
        this.notificationService = notificationService;
    }

    // Displays the main Ticket / Helpdesk list
    @GetMapping("/tickets")
    public String getTicketsPage(Model model, Principal principal) {
        long totalTickets = ticketService.getTicketCount();
        long openTickets = ticketService.getOpenTicketCount();
        List<Ticket> tickets = ticketService.getAllTickets();

        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("tickets", tickets);

        // Attach notification context for the currently logged-in user
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "tickets"; // Renders templates/tickets.html
    }

    // Displays the "File a Support Ticket" Form
    @GetMapping("/tickets/new")
    public String showNewTicketForm(Model model, Principal principal) {
        model.addAttribute("ticket", new Ticket());

        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "new-ticket"; // Renders templates/new-ticket.html
    }

    // Saves a new ticket to the database & notifies the submitter AND admin
    @PostMapping("/tickets/new")
    public String saveTicket(@ModelAttribute("ticket") Ticket ticket, Principal principal, RedirectAttributes redirectAttributes) {
        ticket.setStatus("Open"); // Default starting status
        if (principal != null) {
            ticket.setSubmittedBy(principal.getName());
        }
        
        Ticket savedTicket = ticketService.saveTicket(ticket);

        // 1. Notify the user who created the ticket
        if (principal != null) {
            notificationService.sendNotification(
                principal.getName(),
                "Ticket Created",
                "Your support ticket #" + savedTicket.getId() + " (" + savedTicket.getTitle() + ") was logged."
            );
        }

        // 2. Send notification to admin account
        notificationService.sendNotification(
            "admin",
            "New Ticket Submitted",
            "Ticket #" + savedTicket.getId() + " (" + savedTicket.getTitle() + ") logged by " + (principal != null ? principal.getName() : "User") + "."
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

            redirectAttributes.addFlashAttribute("toastMessage", "Comment posted successfully!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        }

        return "redirect:/tickets/detail/" + id;
    }

    // Processes status update and alerts the ticket creator
    @PostMapping("/tickets/detail/{id}/update-status")
    public String updateTicketStatus(@PathVariable("id") Long id, @RequestParam("status") String status, RedirectAttributes redirectAttributes) {
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

        // Trigger Floating Toast Banner on Ticket Detail
        redirectAttributes.addFlashAttribute("toastMessage", "Ticket #" + id + " status updated to '" + status + "'");
        redirectAttributes.addFlashAttribute("toastType", "success");

        return "redirect:/tickets/detail/" + id;
    }

    // Handles deleting a support ticket entry
    @GetMapping("/tickets/delete/{id}")
    public String deleteTicket(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        ticketService.deleteTicketById(id);

        if (principal != null) {
            notificationService.sendNotification(
                principal.getName(),
                "Ticket Deleted",
                "Support ticket #" + id + " was permanently removed."
            );
        }

        redirectAttributes.addFlashAttribute("toastMessage", "Ticket #" + id + " was permanently deleted.");
        redirectAttributes.addFlashAttribute("toastType", "warning");

        return "redirect:/tickets";
    }
}