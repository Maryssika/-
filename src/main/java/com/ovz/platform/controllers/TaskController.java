package com.ovz.platform.controllers;

import com.ovz.platform.models.EducationalTask;
import com.ovz.platform.services.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/task/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        EducationalTask task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        return "task";
    }
}