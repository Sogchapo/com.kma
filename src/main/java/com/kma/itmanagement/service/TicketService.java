package com.kma.itmanagement.service;

import com.kma.itmanagement.model.Ticket;
import com.kma.itmanagement.model.TicketComment;
import com.kma.itmanagement.repository.TicketCommentRepository;
import com.kma.itmanagement.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;

    public TicketService(TicketRepository ticketRepository, TicketCommentRepository ticketCommentRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketCommentRepository = ticketCommentRepository;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket saveTicket(Ticket ticket) {
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(java.time.LocalDateTime.now());
        }
        return ticketRepository.save(ticket);
    }

    public long getTicketCount() {
        return ticketRepository.count();
    }

    public long getOpenTicketCount() {
        return ticketRepository.findByStatus("Open").size() + ticketRepository.findByStatus("In Progress").size();
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket Id:" + id));
    }

    public void updateTicketStatus(Long id, String status) {
        Ticket ticket = getTicketById(id);
        ticket.setStatus(status);
        ticketRepository.save(ticket);
    }

    // Handles deleting a support ticket by its ID
    public void deleteTicketById(Long id) {
        ticketRepository.deleteById(id);
    }

    // --- Ticket Comments Logic ---

    public List<TicketComment> getCommentsByTicketId(Long ticketId) {
        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    public TicketComment addComment(Long ticketId, String content, String author) {
        Ticket ticket = getTicketById(ticketId);
        TicketComment comment = new TicketComment(content, author, ticket);
        return ticketCommentRepository.save(comment);
    }
}