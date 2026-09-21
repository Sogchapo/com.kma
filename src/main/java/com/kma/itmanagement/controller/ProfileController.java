package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.User;
import com.kma.itmanagement.repository.UserRepository;
import com.kma.itmanagement.service.ActivityLogService;
import com.kma.itmanagement.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public ProfileController(UserRepository userRepository, 
                             NotificationService notificationService,
                             ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model, Principal principal, HttpServletRequest request) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            model.addAttribute("user", user);
            model.addAttribute("notifications", notificationService.getUserNotifications(username));
            model.addAttribute("unreadCount", notificationService.getUnreadCount(username));

            // Log activity: User viewed profile page
            activityLogService.logActivity(
                username,
                "PROFILE",
                "VIEW",
                "Accessed user profile configuration page",
                request.getRemoteAddr()
            );
        }
        return "profile";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile file, 
                               Principal principal, 
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMessage", "Please select an image file to upload.");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/profile";
        }

        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

            // Directory path for uploaded avatars
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Safe extension extraction
            String originalName = file.getOriginalFilename();
            String extension = ".png"; // Default fallback
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir + fileName);

            // Save file to system
            Files.write(filePath, file.getBytes());

            // Save filename to database
            user.setProfileImage(fileName);
            userRepository.save(user);

            // Log activity: Avatar uploaded
            activityLogService.logActivity(
                username,
                "PROFILE",
                "UPDATE_AVATAR",
                "Successfully uploaded and updated user profile avatar",
                request.getRemoteAddr()
            );

            redirectAttributes.addFlashAttribute("toastMessage", "Profile avatar updated successfully!");
            redirectAttributes.addFlashAttribute("toastType", "success");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("toastMessage", "Failed to upload avatar image.");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/avatar/delete")
    public String deleteAvatar(Principal principal, 
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (principal != null) {
            try {
                String username = principal.getName();
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (user.getProfileImage() != null) {
                    // Delete physical file from storage
                    String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
                    Path filePath = Paths.get(uploadDir + user.getProfileImage());
                    Files.deleteIfExists(filePath);

                    // Clear database field
                    user.setProfileImage(null);
                    userRepository.save(user);

                    // Log activity: Avatar deleted
                    activityLogService.logActivity(
                        username,
                        "PROFILE",
                        "DELETE_AVATAR",
                        "Removed user profile avatar image",
                        request.getRemoteAddr()
                    );

                    redirectAttributes.addFlashAttribute("toastMessage", "Profile avatar removed successfully.");
                    redirectAttributes.addFlashAttribute("toastType", "success");
                }
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("toastMessage", "Failed to remove avatar image.");
                redirectAttributes.addFlashAttribute("toastType", "error");
            }
        }
        return "redirect:/profile";
    }
}