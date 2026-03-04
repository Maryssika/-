package com.ovz.platform.repositories.task;

import com.ovz.platform.models.task.EducationalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EducationalTaskRepository extends JpaRepository<EducationalTask, Long> {
    // Поиск заданий по категории (которая соответствует DisabilityType.name().toLowerCase())
    List<EducationalTask> findByCategory(String category);
}