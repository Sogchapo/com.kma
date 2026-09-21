package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.Asset;
import com.kma.itmanagement.service.ActivityLogService;
import com.kma.itmanagement.service.AssetService;
import com.kma.itmanagement.service.ExcelExportService;
import com.kma.itmanagement.service.NotificationService;
import com.kma.itmanagement.service.RequestService;
import com.kma.itmanagement.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
public class AssetController {

    private final AssetService assetService;
    private final TicketService ticketService;
    private final RequestService requestService;
    private final ExcelExportService excelExportService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public AssetController(AssetService assetService,
                           TicketService ticketService,
                           RequestService requestService,
                           ExcelExportService excelExportService,
                           NotificationService notificationService,
                           ActivityLogService activityLogService) {
        this.assetService = assetService;
        this.ticketService = ticketService;
        this.requestService = requestService;
        this.excelExportService = excelExportService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/assets")
    public String getAssetsPage(Model model, Principal principal, HttpServletRequest request) {
        // Use services so calculations match DashboardController 1:1
        List<Asset> assets = assetService.getAllAssets();
        
        model.addAttribute("assets", assets);
        model.addAttribute("assetCount", assetService.getAssetCount());
        model.addAttribute("activeAssets", assetService.getActiveAssetCount());
        model.addAttribute("openTickets", ticketService.getOpenTicketCount());
        model.addAttribute("pendingRequests", requestService.getPendingRequestCount());

        // Populate notification bell context & log view activity
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));

            // Log activity: User viewed asset inventory
            activityLogService.logActivity(
                username,
                "ASSETS",
                "VIEW",
                "Accessed Asset Inventory dashboard page",
                request.getRemoteAddr()
            );
        }

        return "dashboard"; 
    }

    @GetMapping("/assets/export/excel")
    public ResponseEntity<InputStreamResource> exportToExcel(Principal principal, HttpServletRequest request) throws IOException {
        List<Asset> assets = assetService.getAllAssets();
        ByteArrayInputStream in = excelExportService.exportAssetsToExcel(assets);

        // Log activity: User exported asset database
        if (principal != null) {
            activityLogService.logActivity(
                principal.getName(),
                "ASSETS",
                "EXPORT",
                "Exported full asset inventory to Excel (" + assets.size() + " items)",
                request.getRemoteAddr()
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=kma_assets_inventory.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}