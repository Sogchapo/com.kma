package com.kma.itmanagement.controller;

import com.kma.itmanagement.model.User;
import com.kma.itmanagement.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Display list of all users and empty form model for inline registration
    @GetMapping("/users")
    public String showUsersList(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("newUser", new User()); // <-- FIX: Prevents Thymeleaf 500 binding error
        return "users";
    }

    // POST: Submit new user from inline form on users.html
    @PostMapping("/users/new")
    public String registerUser(@ModelAttribute("newUser") User user) {
        if (user.getStatus() == null || user.getStatus().isBlank()) {
            user.setStatus("Active");
        }
        userService.saveUser(user);
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
    public String saveUser(@ModelAttribute("user") User user) {
        userService.saveUser(user);
        return "redirect:/users";
    }

    // GET: Delete a user account
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
}