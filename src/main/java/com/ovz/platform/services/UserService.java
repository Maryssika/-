package com.ovz.platform.services;

import com.ovz.platform.dto.UserRegistrationDto;
import com.ovz.platform.models.user.AccessibilityProfile;
import com.ovz.platform.models.user.DisabilityType;
import com.ovz.platform.models.user.User;
import com.ovz.platform.models.user.UserRole;
import com.ovz.platform.repositories.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
                                  boolean highContrast, boolean subtitles, boolean screenReader,
                                  String fontSize, String colorScheme) {
        User user = findByEmail(email);
        // ... обновление полей ...
        AccessibilityProfile ap = user.getAccessibilityProfile();
        if (ap == null) {
            ap = new AccessibilityProfile();
            user.setAccessibilityProfile(ap);
        }
        ap.setHighContrast(highContrast);
        ap.setSubtitlesEnabled(subtitles);
        ap.setScreenReaderEnabled(screenReader);
        ap.setFontSize(fontSize);
        ap.setColorScheme(colorScheme);
        // ... обновление пароля и имени ...
        userRepository.save(user);
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }
}