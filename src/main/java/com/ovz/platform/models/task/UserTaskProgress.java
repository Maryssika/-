package com.ovz.platform.models.task;

import com.ovz.platform.models.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_task_progress")
public class UserTaskProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private EducationalTask task;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Конструкторы
    public UserTaskProgress() {
    }

    public UserTaskProgress(Long id, User user, EducationalTask task, boolean completed, LocalDateTime completedAt) {
        this.id = id;
        this.user = user;
        this.task = task;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EducationalTask getTask() {
        return task;
    }

    public void setTask(EducationalTask task) {
        this.task = task;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}