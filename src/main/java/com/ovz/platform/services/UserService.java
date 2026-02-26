package com.ovz.platform.services;

import com.ovz.platform.dto.UserRegistrationDto;
import com.ovz.platform.models.AccessibilityProfile;
import com.ovz.platform.models.DisabilityType;
import com.ovz.platform.models.User;
import com.ovz.platform.models.UserRole;
import com.ovz.platform.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // Проверяем совпадение паролей
        if (!registrationDto.isPasswordValid()) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        // Создаем нового пользователя
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setFullName(registrationDto.getFullName());

        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        user.setPassword(encodedPassword);

        // Устанавливаем роль
        try {
            String roleStr = registrationDto.getRole().toUpperCase();
            UserRole role = UserRole.valueOf(roleStr);
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            user.setRole(UserRole.STUDENT); // по умолчанию
        }

        // Устанавливаем тип нарушения (только для учеников)
        if (user.getRole() == UserRole.STUDENT && registrationDto.getDisabilityType() != null) {
            try {
                DisabilityType dt = DisabilityType.valueOf(registrationDto.getDisabilityType().toUpperCase());
                user.setDisabilityType(dt);
            } catch (IllegalArgumentException e) {
                user.setDisabilityType(DisabilityType.OTHER); // по умолчанию
            }
        }

        return userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public void updateUserProfile(String email, String fullName, String newPassword, String confirmPassword,
                                  boolean highContrast, boolean subtitles, String fontSize) {
        User user = findByEmail(email);

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }

        if (newPassword != null && !newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Пароли не совпадают");
            }
            if (newPassword.length() < 8) {
                throw new IllegalArgumentException("Пароль должен содержать минимум 8 символов");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // Сохраняем настройки доступности (если используется AccessibilityProfile)
        if (user.getAccessibilityProfile() == null) {
            user.setAccessibilityProfile(new AccessibilityProfile());
        }
        AccessibilityProfile ap = user.getAccessibilityProfile();
        ap.setHighContrast(highContrast);
        ap.setSubtitlesEnabled(subtitles);
        ap.setFontSize(fontSize);
        // ... сохранить ap, если нужно

        userRepository.save(user);
    }
}