package com.ovz.platform.controllers;

import com.ovz.platform.dto.UserRegistrationDto;
import com.ovz.platform.models.task.EducationalTask;
import com.ovz.platform.models.user.DisabilityType;
import com.ovz.platform.models.user.User;
import com.ovz.platform.models.user.UserRole;
import com.ovz.platform.services.TaskService;
import com.ovz.platform.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final TaskService taskService;

    public AdminController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    // ------------------ Управление пользователями ------------------
    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        model.addAttribute("disabilityTypes", DisabilityType.values());
        model.addAttribute("roles", UserRole.values());
        return "admin/user-form";
    }

    @PostMapping("/users/create")
    public String createUser(@Valid @ModelAttribute("userDto") UserRegistrationDto dto,
                             BindingResult result,
                             RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "admin/user-form";
        }
        try {
            userService.registerUser(dto);
            redirect.addFlashAttribute("successMessage", "Пользователь успешно создан");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole().name());
        dto.setDisabilityType(user.getDisabilityType() != null ? user.getDisabilityType().name() : null);

        model.addAttribute("userDto", dto);
        model.addAttribute("userId", id);
        model.addAttribute("disabilityTypes", DisabilityType.values());
        model.addAttribute("roles", UserRole.values());
        return "admin/user-form";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("userDto") UserRegistrationDto dto,
                             RedirectAttributes redirect) {
        try {
            userService.updateUserByAdmin(id, dto);
            redirect.addFlashAttribute("successMessage", "Пользователь обновлён");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
            redirect.addAttribute("id", id);
            return "redirect:/admin/users/edit/{id}";
        }
        return "redirect:/admin/dashboard";
    }

    // ------------------ Управление заданиями ------------------
    @GetMapping("/tasks")
    public String adminTasks(Model model) {
        List<EducationalTask> tasks = taskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        model.addAttribute("title", "Все задания (администратор)");
        return "admin/tasks";
    }

    @GetMapping("/tasks/create")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new EducationalTask());
        model.addAttribute("disabilityTypes", DisabilityType.values());
        model.addAttribute("title", "Создание задания");
        return "admin/edit-task";
    }

    @PostMapping("/tasks/create")
    public String createTask(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String category,
                             @RequestParam(required = false) Integer difficultyLevel,
                             @RequestParam(required = false) MultipartFile imageFile,
                             @RequestParam(required = false) String alternativeText,
                             RedirectAttributes redirectAttributes) throws IOException {

        EducationalTask task = new EducationalTask();
        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category.toLowerCase());
        task.setDifficultyLevel(difficultyLevel);
        task.setAlternativeText(alternativeText);

        // Логирование получения файла
        System.out.println("Create - received file: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            task.setMediaUrl("/uploads/" + fileName);
            System.out.println("Create - saved as: " + fileName);
        }
        System.out.println("Create - final mediaUrl: " + task.getMediaUrl());

        taskService.saveTask(task);
        redirectAttributes.addFlashAttribute("successMessage", "Задание создано");
        return "redirect:/admin/tasks";
    }

    @GetMapping("/tasks/edit/{id}")
    public String editTask(@PathVariable Long id, Model model) {
        EducationalTask task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("disabilityTypes", DisabilityType.values());
        model.addAttribute("title", "Редактирование задания");
        return "admin/edit-task";
    }

    @PostMapping("/tasks/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String category,
                             @RequestParam(required = false) Integer difficultyLevel,
                             @RequestParam(required = false) MultipartFile imageFile,
                             @RequestParam(required = false) String alternativeText,
                             @RequestParam(required = false, defaultValue = "false") boolean removeImage,
                             RedirectAttributes redirectAttributes) throws IOException {

        EducationalTask task = taskService.getTaskById(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category.toLowerCase());
        task.setDifficultyLevel(difficultyLevel);
        task.setAlternativeText(alternativeText);

        // Логирование
        System.out.println("Update - received file: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));
        System.out.println("Update - removeImage flag: " + removeImage);

        // 1. Обработка удаления
        if (removeImage) {
            deleteOldImage(task.getMediaUrl());
            task.setMediaUrl(null);
            task.setAlternativeText(null);
            System.out.println("Update - image removed");
        }

        // 2. Обработка загрузки нового изображения
        if (imageFile != null && !imageFile.isEmpty()) {
            // Удаляем старое, если есть (даже если removeImage не был отмечен)
            if (task.getMediaUrl() != null) {
                deleteOldImage(task.getMediaUrl());
            }
            String fileName = saveImage(imageFile);
            task.setMediaUrl("/uploads/" + fileName);
            System.out.println("Update - saved new image as: " + fileName);
        }

        System.out.println("Update - final mediaUrl: " + task.getMediaUrl());

        taskService.updateTask(task);
        redirectAttributes.addFlashAttribute("successMessage", "Задание обновлено");
        return "redirect:/admin/tasks";
    }

    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        EducationalTask task = taskService.getTaskById(id);
        // Удаляем привязанное изображение с диска
        deleteOldImage(task.getMediaUrl());
        taskService.deleteTask(id);
        redirectAttributes.addFlashAttribute("successMessage", "Задание удалено");
        return "redirect:/admin/tasks";
    }

    // ------------------ Вспомогательные методы ------------------
    private String saveImage(MultipartFile file) throws IOException {
        String uploadDir = "uploads/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());
        return fileName;
    }

    private void deleteOldImage(String mediaUrl) {
        if (mediaUrl != null && mediaUrl.startsWith("/uploads/")) {
            String fileName = mediaUrl.replace("/uploads/", "");
            Path filePath = Paths.get("uploads/" + fileName);
            try {
                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    System.out.println("Deleted old image: " + fileName);
                }
            } catch (IOException e) {
                System.err.println("Failed to delete image: " + fileName);
                e.printStackTrace();
            }
        }
    }
}