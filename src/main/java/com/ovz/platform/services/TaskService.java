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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        List<EducationalTask> allTasks = getTasksByDisabilityType(user.getDisabilityType());
        List<UserTaskProgress> completedProgress = progressRepository.findByUserAndCompletedTrue(user);
        Set<Long> completedTaskIds = completedProgress.stream()
                .map(p -> p.getTask().getId())
                .collect(Collectors.toSet());
        return allTasks.stream()
                .filter(task -> !completedTaskIds.contains(task.getId()))
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


    // Количество выполненных заданий пользователем
    public long countCompletedTasks(User user) {
        return progressRepository.findByUserAndCompletedTrue(user).size();
    }

    // Общее количество заданий для типа нарушения пользователя
    public long countTotalTasksForUser(User user) {
        return getTasksByDisabilityType(user.getDisabilityType()).size();
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
        List<EducationalTask> allTasks = getTasksByDisabilityType(user.getDisabilityType());
        System.out.println("All tasks for disability " + user.getDisabilityType() + ": " + allTasks.size());

        List<UserTaskProgress> completedProgress = progressRepository.findByUserAndCompletedTrue(user);
        Set<Long> completedIds = completedProgress.stream()
                .map(p -> p.getTask().getId())
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = allTasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("task", task);
            map.put("completed", completedIds.contains(task.getId()));
            return map;
        }).collect(Collectors.toList());

        System.out.println("Result size: " + result.size());
        return result;
    }

    public long countAllTasks() {
        return taskRepository.count();
    }

    public long countAllCompletedTasks() {
        return progressRepository.countByCompletedTrue();
    }
}