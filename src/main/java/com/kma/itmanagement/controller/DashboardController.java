package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.Asset;
import com.kma.itmanagement.repository.UserRepository;
import com.kma.itmanagement.service.AssetService;
import com.kma.itmanagement.service.NotificationService;
import com.kma.itmanagement.service.RequestService;
import com.kma.itmanagement.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class DashboardController {

    private final AssetService assetService;
    private final TicketService ticketService;
    private final NotificationService notificationService;
    private final RequestService requestService;
    private final UserRepository userRepository;

    public DashboardController(AssetService assetService, 
                               TicketService ticketService, 
                               NotificationService notificationService,
                               RequestService requestService,
                               UserRepository userRepository) {
        this.assetService = assetService;
        this.ticketService = ticketService;
        this.notificationService = notificationService;
        this.requestService = requestService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String getDashboard(Model model, Principal principal) {
        long assetCount = assetService.getAssetCount();
        long activeAssets = assetService.getActiveAssetCount();
        long openTickets = ticketService.getOpenTicketCount();
        long pendingRequests = requestService.getPendingRequestCount();
        List<Asset> assets = assetService.getAllAssets();

        model.addAttribute("assetCount", assetCount);
        model.addAttribute("activeAssets", activeAssets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("assets", assets);

        // Populate notification bell context & logged-in user avatar details
        if (principal != null) {
            String username = principal.getName();
            userRepository.findByUsername(username).ifPresent(user -> model.addAttribute("currentUser", user));
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "dashboard"; // Renders templates/dashboard.html
    }

    @GetMapping("/assets/new")
    public String showNewAssetForm(Model model, Principal principal) {
        model.addAttribute("asset", new Asset());

        if (principal != null) {
            String username = principal.getName();
            userRepository.findByUsername(username).ifPresent(user -> model.addAttribute("currentUser", user));
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "new-asset"; // Renders templates/new-asset.html
    }

    @PostMapping("/assets/new")
    public String saveAsset(@ModelAttribute("asset") Asset asset, Principal principal, RedirectAttributes redirectAttributes) {
        Asset savedAsset = assetService.saveAsset(asset);

        // Notify active user of inventory addition
        if (principal != null) {
            notificationService.sendNotification(
                principal.getName(),
                "New Asset Added",
                "Asset '" + savedAsset.getName() + "' (" + savedAsset.getAssetTag() + ") was added to inventory."
            );
        }

        // Trigger Floating Toast Banner on Dashboard
        redirectAttributes.addFlashAttribute("toastMessage", "Asset '" + savedAsset.getName() + "' created successfully!");
        redirectAttributes.addFlashAttribute("toastType", "success");

        return "redirect:/assets";
    }

    // Displays the Edit Asset Form
    @GetMapping("/assets/edit/{id}")
    public String showEditAssetForm(@PathVariable("id") Long id, Model model, Principal principal) {
        Asset asset = assetService.getAssetById(id);
        model.addAttribute("asset", asset);

        if (principal != null) {
            String username = principal.getName();
            userRepository.findByUsername(username).ifPresent(user -> model.addAttribute("currentUser", user));
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));
        }

        return "edit-asset"; // Renders templates/edit-asset.html
    }

    // Processes the updated asset data submission
    @PostMapping("/assets/edit/{id}")
    public String updateAsset(@PathVariable("id") Long id, @ModelAttribute("asset") Asset asset, Principal principal, RedirectAttributes redirectAttributes) {
        assetService.updateAsset(id, asset);

        if (principal != null) {
            notificationService.sendNotification(
                principal.getName(),
                "Asset Updated",
                "Asset #" + id + " details were updated."
            );
        }

        // Trigger Floating Toast Banner on Dashboard
        redirectAttributes.addFlashAttribute("toastMessage", "Asset #" + id + " updated successfully.");
        redirectAttributes.addFlashAttribute("toastType", "success");

        return "redirect:/assets";
    }

    // Handles the removal request of an asset
    @GetMapping("/assets/delete/{id}")
    public String deleteAsset(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        assetService.deleteAssetById(id);

        if (principal != null) {
            notificationService.sendNotification(
                principal.getName(),
                "Asset Deleted",
                "Asset #" + id + " was permanently removed from inventory."
            );
        }

        // Trigger Floating Toast Banner on Dashboard
        redirectAttributes.addFlashAttribute("toastMessage", "Asset #" + id + " was removed from inventory.");
        redirectAttributes.addFlashAttribute("toastType", "warning");

        return "redirect:/assets";
    }
}