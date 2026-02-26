package com.ovz.platform.models;

public enum UserRole {
    STUDENT("Ученик"),
    PARENT("Родитель"),
    TEACHER("Педагог"),
    ADMIN("Администратор");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}