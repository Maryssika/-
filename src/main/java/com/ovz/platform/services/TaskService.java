package com.ovz.platform.services;

import com.ovz.platform.models.user.DisabilityType;
import com.ovz.platform.models.task.EducationalTask;
import com.ovz.platform.models.task.UserTaskProgress;
import com.ovz.platform.models.user.User;
import com.ovz.platform.repositories.task.EducationalTaskRepository;
import com.ovz.platform.repositories.task.UserTaskProgressRepository;
import com.ovz.platform.repositories.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final EducationalTaskRepository taskRepository;
    private final UserTaskProgressRepository progressRepository;
    private final UserRepository userRepository;

    public TaskService(EducationalTaskRepository taskRepository,
                       UserTaskProgressRepository progressRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    // Получить задания по типу нарушения
    public List<EducationalTask> getTasksByDisabilityType(DisabilityType type) {
        if (type == null) {
            return List.of();
        }
        String category = type.name().toLowerCase();
        return taskRepository.findByCategory(category);
    }

    // Получить задание по ID
    public EducationalTask getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Задание с id " + id + " не найдено"));
    }

    // Получить невыполненные задания для пользователя
    public List<EducationalTask> getUncompletedTasksForUser(User user) {
        List<EducationalTask> personalized = getTasksByDisabilityType(user.getDisabilityType());
        List<EducationalTask> assigned = getAssignedTasksForStudent(user);

        Map<Long, EducationalTask> uniqueTasks = new LinkedHashMap<>();
        for (EducationalTask t : personalized) uniqueTasks.put(t.getId(), t);
        for (EducationalTask t : assigned) uniqueTasks.put(t.getId(), t);

        List<UserTaskProgress> completedProgress = progressRepository.findByUserAndCompletedTrue(user);
        Set<Long> completedIds = completedProgress.stream()
                .map(p -> p.getTask().getId())
                .collect(Collectors.toSet());

        return uniqueTasks.values().stream()
                .filter(t -> !completedIds.contains(t.getId()))
                .collect(Collectors.toList());
    }

    public List<EducationalTask> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public void saveTask(EducationalTask task) {
        taskRepository.save(task);
    }

    @Transactional
    public void updateTask(EducationalTask task) {
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional
    public void markTaskAsCompleted(User user, EducationalTask task) {
        // Не начисляем звёзды повторно, если задание уже выполнено
        if (!isTaskCompleted(user, task)) {
            UserTaskProgress progress = new UserTaskProgress();
            progress.setUser(user);
            progress.setTask(task);
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            progressRepository.save(progress);

            // Начисляем 1 звезду за задание
            user.setStars(user.getStars() + 1);
            userRepository.save(user);
        }
    }

    // Назначить задание ученику
    @Transactional
    public void assignTaskToStudent(User student, Long taskId) {
        EducationalTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задание не найдено"));
        if (!student.getAssignedTasks().contains(task)) {
            student.getAssignedTasks().add(task);
            userRepository.save(student);
        }
    }

    // Получить назначенные задания ученика
    public List<EducationalTask> getAssignedTasksForStudent(User student) {
        return student.getAssignedTasks();
    }

    // Отметить выполнение назначенного задания (удаляем из списка)
    @Transactional
    public void completeAssignedTask(User student, EducationalTask task) {
        if (student.getAssignedTasks().remove(task)) {
            userRepository.save(student);
        }
    }


    // Количество выполненных заданий пользователем
    public long countCompletedTasks(User user) {
        return progressRepository.findByUserAndCompletedTrue(user).size();
    }

    // Общее количество заданий для типа нарушения пользователя
    public long countTotalTasksForUser(User user) {
        List<EducationalTask> personalized = getTasksByDisabilityType(user.getDisabilityType());
        List<EducationalTask> assigned = getAssignedTasksForStudent(user);
        Set<Long> uniqueIds = new HashSet<>();
        for (EducationalTask t : personalized) uniqueIds.add(t.getId());
        for (EducationalTask t : assigned) uniqueIds.add(t.getId());
        return uniqueIds.size();

    }

    // Проверить, выполнено ли задание пользователем
    public boolean isTaskCompleted(User user, EducationalTask task) {
        return progressRepository.existsByUserAndTaskAndCompletedTrue(user, task);
    }

    public List<Map<String, Object>> getTasksWithStatus(User user) {
        if (user.getDisabilityType() == null) {
            System.out.println("У ученика не указан тип нарушения, возвращаем пустой список");
            return List.of();
        }
        List<EducationalTask> personalized = getTasksByDisabilityType(user.getDisabilityType());
        List<EducationalTask> assigned = getAssignedTasksForStudent(user);

        // Объединяем уникальные задания по ID
        Map<Long, EducationalTask> uniqueTasks = new LinkedHashMap<>();
        for (EducationalTask t : personalized) uniqueTasks.put(t.getId(), t);
        for (EducationalTask t : assigned) uniqueTasks.put(t.getId(), t);

        // Получаем ID выполненных заданий
        List<UserTaskProgress> completedProgress = progressRepository.findByUserAndCompletedTrue(user);
        Set<Long> completedIds = completedProgress.stream()
                .map(p -> p.getTask().getId())
                .collect(Collectors.toSet());

        // Формируем результат
        List<Map<String, Object>> result = new ArrayList<>();
        for (EducationalTask task : uniqueTasks.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("task", task);
            map.put("completed", completedIds.contains(task.getId()));
            result.add(map);
        }
        return result;
    }

    public long countAllTasks() {
        return taskRepository.count();
    }

    public long countAllCompletedTasks() {
        return progressRepository.countByCompletedTrue();
    }
}