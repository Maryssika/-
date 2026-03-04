package com.ovz.platform.services;

import com.ovz.platform.models.user.DisabilityType;
import com.ovz.platform.models.task.EducationalTask;
import com.ovz.platform.repositories.task.EducationalTaskRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {

    private final EducationalTaskRepository taskRepository;

    public TaskService(EducationalTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Получить список заданий, подходящих для указанного типа нарушения.
     * Категория задания хранится в нижнем регистре и совпадает с именем enum.
     */
    public List<EducationalTask> getTasksByDisabilityType(DisabilityType type) {
        if (type == null) {
            return List.of(); // или все задания?
        }
        String category = type.name().toLowerCase();
        return taskRepository.findByCategory(category);
    }
    public EducationalTask getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Задание с id " + id + " не найдено"));
    }
}