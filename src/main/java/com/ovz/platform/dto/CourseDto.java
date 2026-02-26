package com.ovz.platform.dto;

public class CourseDto {
    private String title;
    private String description;
    private String color;      // primary, success, info, warning, danger
    private Integer completed;
    private Integer total;

    // Конструктор
    public CourseDto(String title, String description, String color, Integer completed, Integer total) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.completed = completed;
        this.total = total;
    }

    // Геттеры (обязательно!)
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public Integer getCompleted() { return completed; }
    public Integer getTotal() { return total; }
}