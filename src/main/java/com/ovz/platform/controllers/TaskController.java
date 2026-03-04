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

        EducationalTask task = taskService.getTaskById(id);
        model.addAttribute("task", task);

        // Получаем все персонализированные задания для этого ученика
        List<EducationalTask> allTasks = taskService.getTasksByDisabilityType(user.getDisabilityType());
        model.addAttribute("allTasks", allTasks);

        // Находим индекс текущего задания и следующее
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
                               Model model) {
        // Здесь можно сохранить ответ в базу, проверить правильность и т.д.
        // Пока просто передаём сообщение об успехе
        model.addAttribute("message", "Ответ принят! Спасибо.");
        return "redirect:/task/" + id + "?success";
    }
}