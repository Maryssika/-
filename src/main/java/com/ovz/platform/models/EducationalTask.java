package com.ovz.platform.models;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "educational_tasks")
@Data
public class EducationalTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String category; // visual, auditory, motor, cognitive
    private Integer difficultyLevel;
    private String mediaUrl;
    private String alternativeText;

//    @Enumerated(EnumType.STRING)
//    private TaskType type; // DRAG_DROP, MULTIPLE_CHOICE, MATCHING, etc.
}
