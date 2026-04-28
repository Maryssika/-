package com.ovz.platform.models.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "disability_type")
    private DisabilityType disabilityType;

    @OneToOne(cascade = CascadeType.ALL)
    private AccessibilityProfile accessibilityProfile;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> children = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> students = new ArrayList<>();

    @Column(name = "stars", columnDefinition = "int default 0")
    private int stars = 0;

    // --- Геттеры и сеттеры ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public DisabilityType getDisabilityType() { return disabilityType; }
    public void setDisabilityType(DisabilityType disabilityType) { this.disabilityType = disabilityType; }

    public AccessibilityProfile getAccessibilityProfile() { return accessibilityProfile; }
    public void setAccessibilityProfile(AccessibilityProfile accessibilityProfile) { this.accessibilityProfile = accessibilityProfile; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getParent() { return parent; }
    public void setParent(User parent) { this.parent = parent; }

    public List<User> getChildren() { return children; }
    public void setChildren(List<User> children) { this.children = children; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public List<User> getStudents() { return students; }
    public void setStudents(List<User> students) { this.students = students; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}