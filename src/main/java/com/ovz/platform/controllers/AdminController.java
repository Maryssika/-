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
import org.springframework.ui.Model; // правильный импорт!
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // можно вынести общую проверку на уровень класса
public class AdminController {

    private final UserService userService;
    private final TaskService taskService;

    public AdminController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

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
        // Пароль не заполняем

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

    @GetMapping("/tasks")
    public String adminTasks(Model model) {
        List<EducationalTask> tasks = taskService.getAllTasks();
        model.addAttribute("tasks", tasks);
        model.addAttribute("title", "Все задания (администратор)");
        return "admin/tasks";
    }

    // Форма создания задания (можно перенаправить на существующую учительскую или сделать свою)
    @GetMapping("/tasks/create")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new EducationalTask());
        model.addAttribute("disabilityTypes", DisabilityType.values());
        model.addAttribute("title", "Создание задания");
        return "admin/edit-task";
    }

    // Обработка создания
    @PostMapping("/tasks/create")
    public String createTask(@ModelAttribute EducationalTask task,
                             @RequestParam String category,
                             RedirectAttributes redirectAttributes) {
        task.setCategory(category.toLowerCase());
        taskService.saveTask(task);
        redirectAttributes.addFlashAttribute("successMessage", "Задание создано");
        return "redirect:/admin/tasks";
    }

    // Форма редактирования
    @GetMapping("/tasks/edit/{id}")
    public String editTask(@PathVariable Long id, Model model) {
        EducationalTask task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("disabilityTypes", DisabilityType.values());
        model.addAttribute("title", "Редактирование задания");
        return "admin/edit-task";
    }

    // Сохранение изменений
    @PostMapping("/tasks/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute EducationalTask task,
                             @RequestParam String category,
                             RedirectAttributes redirectAttributes) {
        task.setId(id);
        task.setCategory(category.toLowerCase());
        taskService.updateTask(task);
        redirectAttributes.addFlashAttribute("successMessage", "Задание обновлено");
        return "redirect:/admin/tasks";
    }

    // Удаление
    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.deleteTask(id);
        redirectAttributes.addFlashAttribute("successMessage", "Задание удалено");
        return "redirect:/admin/tasks";
    }
}

