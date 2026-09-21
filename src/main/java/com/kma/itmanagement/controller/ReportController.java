package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.ActivityLog;
import com.kma.itmanagement.model.Asset;
import com.kma.itmanagement.model.Request;
import com.kma.itmanagement.model.Ticket;
import com.kma.itmanagement.model.User;
import com.kma.itmanagement.repository.ActivityLogRepository;
import com.kma.itmanagement.repository.AssetRepository;
import com.kma.itmanagement.repository.RequestRepository;
import com.kma.itmanagement.repository.TicketRepository;
import com.kma.itmanagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class ReportController {

    private final TicketRepository ticketRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final RequestRepository requestRepository;

    public ReportController(TicketRepository ticketRepository, 
                            ActivityLogRepository activityLogRepository, 
                            UserRepository userRepository,
                            AssetRepository assetRepository,
                            RequestRepository requestRepository) {
        this.ticketRepository = ticketRepository;
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping("/reports/download/assets")
    public void downloadAssetsReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"assets_report.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("ID,Asset Name,Type,Asset Tag,Location,Status");
        
        List<Asset> assets = assetRepository.findAll();
        for (Asset a : assets) {
            writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                a.getId() != null ? a.getId() : 0L,
                escapeCsv(a.getName()),
                escapeCsv(a.getType()),
                escapeCsv(a.getAssetTag()),
                escapeCsv(a.getLocation()),
                escapeCsv(a.getStatus())
            );
        }
        writer.flush();
    }

    @GetMapping("/reports/download/item-requests")
    public void downloadItemRequestsReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"item_requests_report.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("ID,Item Requested,Requested By,Department,Status,Reason,Requested At");
        
        List<Request> requests = requestRepository.findAll();
        for (Request r : requests) {
            writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                r.getId() != null ? r.getId() : 0L,
                escapeCsv(r.getItemRequested()),
                escapeCsv(r.getRequestedBy()),
                escapeCsv(r.getDepartment()),
                escapeCsv(r.getStatus() != null ? r.getStatus().name() : ""),
                escapeCsv(r.getReason()),
                r.getRequestedAt() != null ? r.getRequestedAt().toString() : ""
            );
        }
        writer.flush();
    }

    @GetMapping("/reports/download/helpdesk")
    public void downloadHelpdeskReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"helpdesk_tickets_report.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("ID,Title,SubmittedBy,Priority,Status,Created At");
        
        List<Ticket> tickets = ticketRepository.findAll();
        for (Ticket t : tickets) {
            writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                t.getId() != null ? t.getId() : 0L,
                escapeCsv(t.getTitle()),
                escapeCsv(t.getSubmittedBy()),
                escapeCsv(t.getPriority()),
                escapeCsv(t.getStatus()),
                t.getCreatedAt() != null ? t.getCreatedAt().toString() : ""
            );
        }
        writer.flush();
    }

    @GetMapping("/reports/download/activity-logs")
    public void downloadActivityLogsReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"activity_logs_report.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("ID,Username,Module,Action,Description,IP Address,Timestamp");
        
        List<ActivityLog> logs = activityLogRepository.findAll();
        for (ActivityLog l : logs) {
            writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                l.getId() != null ? l.getId() : 0L,
                escapeCsv(l.getUsername()),
                escapeCsv(l.getModule()),
                escapeCsv(l.getAction()),
                escapeCsv(l.getDetails()),
                escapeCsv(l.getIpAddress()),
                l.getTimestamp() != null ? l.getTimestamp().toString() : ""
            );
        }
        writer.flush();
    }

    @GetMapping("/reports/download/users")
    public void downloadUsersReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"user_management_report.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("ID,Username,Role,Department,Email");
        
        List<User> users = userRepository.findAll();
        for (User u : users) {
            writer.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                u.getId() != null ? u.getId() : 0L,
                escapeCsv(u.getUsername()),
                escapeCsv(u.getRole()),
                escapeCsv(u.getDepartment()),
                escapeCsv(u.getEmail())
            );
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}