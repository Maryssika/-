package com.ovz.platform.controllers;

import com.ovz.platform.dto.CourseDto;
import com.ovz.platform.dto.UserRegistrationDto;
import com.ovz.platform.models.DisabilityType;
import com.ovz.platform.models.EducationalTask;
import com.ovz.platform.models.User;
import com.ovz.platform.models.UserRole;
import com.ovz.platform.services.TaskService;
import com.ovz.platform.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.List;

@Controller
public class MainController {

    private final UserService userService;
    private final TaskService taskService;

    public MainController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Образовательная платформа для детей с ОВЗ");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "О проекте");
        return "about";
    }

    @GetMapping("/features")
    public String features(Model model) {
        model.addAttribute("title", "Возможности платформы");
        return "features";
    }

    @GetMapping("/learning")
    public String learning(Model model) {
        model.addAttribute("title", "Начать обучение");
        return "learning";
    }

    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("title", "Демо-версия");
        return "demo";
    }

    @GetMapping("/team")
    public String team(Model model) {
        model.addAttribute("title", "Наша команда");
        return "team";
    }

    @GetMapping("/contacts")
    public String contacts(Model model) {
        model.addAttribute("title", "Контакты");
        return "contacts";
    }

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout) {
        model.addAttribute("title", "Вход в систему");
        if (error != null) {
            model.addAttribute("error", "Неверный email или пароль");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Регистрация");
        model.addAttribute("userDto", new UserRegistrationDto());
        model.addAttribute("disabilityTypes", DisabilityType.values()); // <-- добавляем
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
                                      BindingResult result,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Регистрация");
            model.addAttribute("disabilityTypes", DisabilityType.values()); // <-- и здесь тоже
            return "register";
        }

        try {
            User user = userService.registerUser(userDto);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрация успешна! Теперь вы можете войти в систему.");

            return "redirect:/registration-success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userDto", userDto);
            model.addAttribute("title", "Регистрация");
            return "register";
        }
    }

    @GetMapping("/registration-success")
    public String registrationSuccess(Model model) {
        model.addAttribute("title", "Регистрация успешна");
        return "registration-success";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("title", "Мой профиль");
        model.addAttribute("user", user);
        return "profile";
    }

    // Ролевые страницы
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("title", "Панель администратора");
        return "admin/dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Model model) {
        model.addAttribute("title", "Панель учителя");
        return "teacher/dashboard";
    }

    @GetMapping("/student/dashboard")
        public String studentDashboard(Model model) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }

            String email = auth.getName();
            User user;
            try {
                user = userService.findByEmail(email);
            } catch (UsernameNotFoundException e) {
                return "redirect:/login?error=user_not_found";
            }

            // Проверяем, что пользователь – ученик
            if (user.getRole() != UserRole.STUDENT) {
                return "redirect:/profile"; // или другое
            }

            // Основные атрибуты
            model.addAttribute("fullName", user.getFullName() != null ? user.getFullName() : "Ученик");
            model.addAttribute("tasksCompleted", 3); // заглушка
            model.addAttribute("tasksTotal", 5);
            model.addAttribute("studyMinutes", 45);
            model.addAttribute("studyGoalMinutes", 60);
            model.addAttribute("courses", getDummyCourses()); // заглушка

            // Задания, персонализированные под тип нарушения
            DisabilityType dt = user.getDisabilityType();
            List<EducationalTask> tasks = taskService.getTasksByDisabilityType(dt);
            model.addAttribute("personalizedTasks", tasks);

            return "student/dashboard";
        }

    @GetMapping("/parent/dashboard")
    public String parentDashboard(Model model) {
        model.addAttribute("title", "Панель родителя");
        return "parent/dashboard";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = auth.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        return "settings";
    }


    @PostMapping("/settings/update")
    public String updateSettings(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam(required = false) boolean highContrast,
            @RequestParam(required = false) boolean subtitles,
            @RequestParam(required = false) String fontSize,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = auth.getName();
        try {
            userService.updateUserProfile(email, fullName, newPassword, confirmPassword, highContrast, subtitles, fontSize);
            redirectAttributes.addFlashAttribute("successMessage", "Настройки успешно обновлены!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        // Здесь можно загрузить реальные уведомления из БД
        return "notifications";
    }

    // Вспомогательный метод — заглушка для курсов
    private List<CourseDto> getDummyCourses() {
        return List.of(
                new CourseDto("Математика", "Счёт, простые примеры, логические задачки", "primary", 3, 5),
                new CourseDto("Чтение", "Крупный текст, подсветка строки, озвучка слов", "success", 2, 4),
                new CourseDto("Окружающий мир", "Животные, растения, времена года", "info", 1, 3),
                new CourseDto("Творчество", "Рисование, музыка, поделки", "warning", 0, 2),
                new CourseDto("Игровая пауза", "Развивающие игры, головоломки", "danger", null, null)
        );
    }

}