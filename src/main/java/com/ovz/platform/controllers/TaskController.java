package com.ovz.platform.controllers;

import com.ovz.platform.models.task.EducationalTask;
import com.ovz.platform.models.user.User;
import com.ovz.platform.services.TaskService;
import com.ovz.platform.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/task/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(auth.getName());

        // Получаем задание до проверки
        EducationalTask task = taskService.getTaskById(id);

        // Проверяем, не выполнено ли уже
        if (taskService.isTaskCompleted(user, task)) {
            return "redirect:/student/dashboard?alreadyCompleted";
        }

        // Настройки доступности
        model.addAttribute("highContrast", user.getAccessibilityProfile() != null ? user.getAccessibilityProfile().getHighContrast() : false);
        model.addAttribute("fontSize", user.getAccessibilityProfile() != null ? user.getAccessibilityProfile().getFontSize() : "medium");
        model.addAttribute("task", task);

        // Все задания для данного типа нарушения
        List<EducationalTask> allTasks = taskService.getTasksByDisabilityType(user.getDisabilityType());
        model.addAttribute("allTasks", allTasks);

        // Индекс текущего и следующее задание
        int currentIndex = -1;
        for (int i = 0; i < allTasks.size(); i++) {
            if (allTasks.get(i).getId().equals(id)) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex != -1 && currentIndex + 1 < allTasks.size()) {
            model.addAttribute("nextTaskId", allTasks.get(currentIndex + 1).getId());
        }

        return "task";
    }

    @PostMapping("/task/{id}/answer")
    public String submitAnswer(@PathVariable Long id,
                               @RequestParam("answer") String answer,
                               RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(auth.getName());
        EducationalTask task = taskService.getTaskById(id);

        // Отмечаем задание выполненным
        taskService.markTaskAsCompleted(user, task);

        redirectAttributes.addFlashAttribute("successMessage", "Задание выполнено! Молодец!");
        return "redirect:/student/dashboard";
    }
}