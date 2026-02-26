package com.ovz.platform.models;

public enum DisabilityType {
    VISUAL("Нарушение зрения"),
    AUDITORY("Нарушение слуха"),
    MOTOR("Нарушение опорно-двигательного аппарата"),
    COGNITIVE("Нарушение интеллекта / ЗПР"),
    SPEECH("Нарушение речи"),
    OTHER("Другие нарушения");

    private final String displayName;

    DisabilityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}