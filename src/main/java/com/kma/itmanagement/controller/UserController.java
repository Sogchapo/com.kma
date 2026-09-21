package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.User;
import com.kma.itmanagement.service.ActivityLogService;
import com.kma.itmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class UserController {

    private final UserService userService;
    private final ActivityLogService activityLogService;

    public UserController(UserService userService, ActivityLogService activityLogService) {
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    // Display list of all users and empty form model for inline registration
    @GetMapping("/users")
    public String showUsersList(Model model, Principal principal, HttpServletRequest request) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("newUser", new User()); // Prevents Thymeleaf 500 binding error

        // Optional: Log when admin views user management page
        if (principal != null) {
            activityLogService.logActivity(
                principal.getName(),
                "USER_MGMT",
                "VIEW",
                "Accessed User Management directory",
                request.getRemoteAddr()
            );
        }

        return "users";
    }

    // POST: Submit new user from inline form on users.html
    @PostMapping("/users/new")
    public String registerUser(@ModelAttribute("newUser") User user, 
                               Principal principal, 
                               HttpServletRequest request) {
        if (user.getStatus() == null || user.getStatus().isBlank()) {
            user.setStatus("Active");
        }
        userService.saveUser(user);

        // Log activity: Admin created a new user account
        if (principal != null) {
            activityLogService.logActivity(
                principal.getName(),
                "USER_MGMT",
                "CREATE_USER",
                "Created new staff user account: " + user.getUsername(),
                request.getRemoteAddr()
            );
        }

        return "redirect:/users";
    }

    // GET: Display the edit form for an existing user
    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getUserById(id);
        user.setPassword(""); // Clear encoded password before rendering edit view
        model.addAttribute("user", user);
        return "user-form";
    }

    // POST: Save updated user details from user-form.html
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") User user, 
                           Principal principal, 
                           HttpServletRequest request) {
        userService.saveUser(user);

        // Log activity: Admin updated a user account
        if (principal != null) {
            activityLogService.logActivity(
                principal.getName(),
                "USER_MGMT",
                "UPDATE_USER",
                "Updated user details for account: " + user.getUsername(),
                request.getRemoteAddr()
            );
        }

        return "redirect:/users";
    }

    // GET: Delete a user account
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, 
                             Principal principal, 
                             HttpServletRequest request) {
        userService.deleteUser(id);

        // Log activity: Admin deleted a user account
        if (principal != null) {
            activityLogService.logActivity(
                principal.getName(),
                "USER_MGMT",
                "DELETE_USER",
                "Deleted user account ID: " + id,
                request.getRemoteAddr()
            );
        }

        return "redirect:/users";
    }
}