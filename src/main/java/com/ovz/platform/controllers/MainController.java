package com.ovz.platform.controllers;

import com.ovz.platform.dto.CourseDto;
import com.ovz.platform.dto.UserRegistrationDto;
import com.ovz.platform.models.user.AccessibilityProfile;
import com.ovz.platform.models.user.DisabilityType;
import com.ovz.platform.models.task.EducationalTask;
import com.ovz.platform.models.user.User;
import com.ovz.platform.models.user.UserRole;
import com.ovz.platform.services.TaskService;
import com.ovz.platform.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/demo/audio-task")
    public String audioTaskDemo(Model model) {
        model.addAttribute("title", "Аудиальное задание");
        model.addAttribute("taskName", "Угадай животное по звуку");
        model.addAttribute("taskDescription", "Послушайте звук и выберите правильное животное");
        model.addAttribute("category", "Для слабовидящих детей");
        return "demo/audio-task";
    }

    @GetMapping("/demo/color-task")
    public String colorTaskDemo(Model model) {
        model.addAttribute("title", "Цветовое задание");
        model.addAttribute("taskName", "Сортировка предметов по цвету");
        model.addAttribute("taskDescription", "Перетащите предметы в корзины соответствующего цвета");
        model.addAttribute("category", "Для всех категорий");
        return "demo/color-task";
    }

    @GetMapping("/demo/puzzle-task")
    public String puzzleTaskDemo(Model model) {
        model.addAttribute("title", "Собери картинку");
        model.addAttribute("taskName", "Собери картинку");
        model.addAttribute("taskDescription", "Расставьте элементы в правильном порядке");
        model.addAttribute("category", "Для развития логики");
        return "demo/puzzle-task";
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
        model.addAttribute("userDto", new UserRegistrationDto());
        model.addAttribute("disabilityTypes", DisabilityType.values());
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
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }
        try {
            User user = userService.findByEmail(authentication.getName());
            model.addAttribute("user", user);
            return "profile";
        } catch (UsernameNotFoundException e) {
            return "redirect:/login";
        }
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
        User user = userService.findByEmail(email);
        if (user.getRole() != UserRole.STUDENT) {
            return "redirect:/profile";
        }

        model.addAttribute("fullName", user.getFullName() != null ? user.getFullName() : "Ученик");

        // Реальная статистика
        long tasksCompleted = taskService.countCompletedTasks(user);
        long tasksTotal = taskService.countTotalTasksForUser(user);
        model.addAttribute("tasksCompleted", tasksCompleted);
        model.addAttribute("tasksTotal", tasksTotal);
        model.addAttribute("studyMinutes", 45); // заглушка
        model.addAttribute("studyGoalMinutes", 60);

        // Настройки доступности
        AccessibilityProfile ap = user.getAccessibilityProfile();
        model.addAttribute("highContrast", ap != null ? ap.getHighContrast() : false);
        model.addAttribute("fontSize", ap != null ? ap.getFontSize() : "medium");
        model.addAttribute("subtitles", ap != null ? ap.getSubtitlesEnabled() : false);
        model.addAttribute("screenReader", ap != null ? ap.getScreenReaderEnabled() : false); // <-- добавить эту

        // Только невыполненные задания
        List<EducationalTask> tasks = taskService.getUncompletedTasksForUser(user);
        model.addAttribute("personalizedTasks", tasks);

        return "student/dashboard";
    }


    @GetMapping("/parent/dashboard")
    public String parentDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = auth.getName();
        User parent = userService.findByEmail(email);

        // Проверяем, что пользователь действительно родитель
        if (parent.getRole() != UserRole.PARENT) {
            return "redirect:/profile";
        }

        // Получаем детей родителя
        List<User> children = userService.getChildren(parent);

        // Для каждого ребёнка получаем статистику по заданиям
        Map<User, Map<String, Object>> childrenStats = new HashMap<>();
        for (User child : children) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("tasksCompleted", taskService.countCompletedTasks(child));
            stats.put("tasksTotal", taskService.countTotalTasksForUser(child));
            stats.put("studyMinutes", 45); // можно заменить реальными данными
            childrenStats.put(child, stats);
        }

        model.addAttribute("parent", parent);
        model.addAttribute("childrenStats", childrenStats);
        model.addAttribute("title", "Панель родителя");

        return "parent/dashboard";
    }

    @GetMapping("/parent/child/{id}")
    public String parentChildDetails(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = auth.getName();
        User parent = userService.findByEmail(email);

        // Проверяем роль родителя
        if (parent.getRole() != UserRole.PARENT) {
            return "redirect:/profile";
        }

        // Получаем ребёнка с проверкой принадлежности
        User child;
        try {
            child = userService.getChildForParent(parent, id);
        } catch (SecurityException e) {
            return "redirect:/parent/dashboard?error=access_denied";
        }

        // Получаем детальную статистику
        long tasksCompleted = taskService.countCompletedTasks(child);
        long tasksTotal = taskService.countTotalTasksForUser(child);
        List<Map<String, Object>> tasksWithStatus = taskService.getTasksWithStatus(child);

        model.addAttribute("child", child);
        model.addAttribute("tasksCompleted", tasksCompleted);
        model.addAttribute("tasksTotal", tasksTotal);
        model.addAttribute("tasksWithStatus", tasksWithStatus);
        model.addAttribute("title", "Профиль ученика");

        return "parent/child";
    }

    @GetMapping("/parent/add-child")
    public String showAddChildForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        User parent = userService.findByEmail(auth.getName());
        if (parent.getRole() != UserRole.PARENT) {
            return "redirect:/profile";
        }
        model.addAttribute("parent", parent);
        return "parent/add-child";
    }

    @PostMapping("/parent/add-child")
    public String addChild(@RequestParam String childEmail, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            userService.addChildToParent(auth.getName(), childEmail);
            redirectAttributes.addFlashAttribute("successMessage", "Ребёнок успешно привязан!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/parent/dashboard";
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
            @RequestParam(required = false) boolean screenReader,
            @RequestParam(required = false) String fontSize,
            @RequestParam(required = false) String colorScheme,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        String email = auth.getName();

        try {
            userService.updateUserProfile(email, fullName, newPassword, confirmPassword,
                    highContrast, subtitles, screenReader, fontSize, colorScheme);
            redirectAttributes.addFlashAttribute("successMessage", "Настройки успешно обновлены!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/settings"; // при ошибке остаёмся на странице настроек
        }

        return "redirect:/profile"; // <--- ИЗМЕНЕНО
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

    @PostMapping("/settings/quick-update")
    @ResponseBody
    public ResponseEntity<?> quickUpdate(@RequestBody Map<String, Object> updates) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Не авторизован");
            }
            String email = auth.getName();
            User user = userService.findByEmail(email);
            AccessibilityProfile ap = user.getAccessibilityProfile();
            if (ap == null) {
                ap = new AccessibilityProfile();
                user.setAccessibilityProfile(ap);
            }
            if (updates.containsKey("highContrast")) {
                ap.setHighContrast((Boolean) updates.get("highContrast"));
            }
            if (updates.containsKey("fontSize")) {
                ap.setFontSize((String) updates.get("fontSize"));
            }
            userService.save(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace(); // для отладки
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка сервера");
        }
    }
}