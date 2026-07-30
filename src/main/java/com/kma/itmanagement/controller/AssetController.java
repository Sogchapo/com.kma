package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.Asset;
import com.kma.itmanagement.service.AssetService;
import com.kma.itmanagement.service.ExcelExportService;
import com.kma.itmanagement.service.NotificationService;
import com.kma.itmanagement.service.RequestService;
import com.kma.itmanagement.service.TicketService;
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

    public AssetController(AssetService assetService,
                           TicketService ticketService,
                           RequestService requestService,
                           ExcelExportService excelExportService,
                           NotificationService notificationService) {
        this.assetService = assetService;
        this.ticketService = ticketService;
        this.requestService = requestService;
        this.excelExportService = excelExportService;
        this.notificationService = notificationService;
    }

    @GetMapping("/assets")
    public String getAssetsPage(Model model, Principal principal) {
        // Use services so calculations match DashboardController 1:1
        List<Asset> assets = assetService.getAllAssets();
        
        model.addAttribute("assets", assets);
        model.addAttribute("assetCount", assetService.getAssetCount());
        model.addAttribute("activeAssets", assetService.getActiveAssetCount());
        model.addAttribute("openTickets", ticketService.getOpenTicketCount());
        model.addAttribute("pendingRequests", requestService.getPendingRequestCount());

        // Populate notification bell context
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "dashboard"; 
    }

    @GetMapping("/assets/export/excel")
    public ResponseEntity<InputStreamResource> exportToExcel() throws IOException {
        List<Asset> assets = assetService.getAllAssets();
        ByteArrayInputStream in = excelExportService.exportAssetsToExcel(assets);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=kma_assets_inventory.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}