package com.ovz.platform.models.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "accessibility_profiles")
@Data
public class AccessibilityProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean highContrast;
    private String fontSize; // small, medium, large, x-large
    private Boolean subtitlesEnabled;
    private Boolean screenReaderEnabled;
    private String colorScheme; // default, dark, blue, etc.

    // Геттеры и сеттеры (если @Data не работает)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getHighContrast() {
        return highContrast;
    }

    public void setHighContrast(Boolean highContrast) {
        this.highContrast = highContrast;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public Boolean getSubtitlesEnabled() {
        return subtitlesEnabled;
    }

    public void setSubtitlesEnabled(Boolean subtitlesEnabled) {
        this.subtitlesEnabled = subtitlesEnabled;
    }

    public Boolean getScreenReaderEnabled() {
        return screenReaderEnabled;
    }

    public void setScreenReaderEnabled(Boolean screenReaderEnabled) {
        this.screenReaderEnabled = screenReaderEnabled;
    }

    public String getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }
}