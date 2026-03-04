package com.ovz.platform.models.task;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "educational_tasks")
@Data
public class EducationalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "alternative_text")
    private String alternativeText;

    // Конструкторы
    public EducationalTask() {
    }

    public EducationalTask(String title, String description, String category,
                           Integer difficultyLevel, String mediaUrl, String alternativeText) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficultyLevel = difficultyLevel;
        this.mediaUrl = mediaUrl;
        this.alternativeText = alternativeText;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getAlternativeText() {
        return alternativeText;
    }

    public void setAlternativeText(String alternativeText) {
        this.alternativeText = alternativeText;
    }

    @Override
    public String toString() {
        return "EducationalTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", difficultyLevel=" + difficultyLevel +
                '}';
    }
}
