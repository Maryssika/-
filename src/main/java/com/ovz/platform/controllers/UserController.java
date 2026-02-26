package com.ovz.platform.controllers;

import com.ovz.platform.dto.UserRegistrationDto;
import com.ovz.platform.models.User;
import com.ovz.platform.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public String editUser(@PathVariable Long id, Model model) {
        // Реализация редактирования пользователя
        return "users/edit";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute UserRegistrationDto userDto,
                             RedirectAttributes redirectAttributes) {
        // Реализация обновления пользователя
        redirectAttributes.addFlashAttribute("successMessage", "Профиль обновлен");
        return "redirect:/profile";
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model) {
        // Реализация списка пользователей для администратора
        return "users/list";
    }
}