package com.ovz.platform.repositories.task;

import com.ovz.platform.models.task.EducationalTask;
import com.ovz.platform.models.task.UserTaskProgress;
import com.ovz.platform.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserTaskProgressRepository extends JpaRepository<UserTaskProgress, Long> {
    List<UserTaskProgress> findByUserAndCompletedTrue(User user);
    List<UserTaskProgress> findByUser(User user);
    boolean existsByUserAndTaskAndCompletedTrue(User user, EducationalTask task);
}