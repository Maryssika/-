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

import java.util.List;

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
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        if (!registrationDto.isPasswordValid()) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setFullName(registrationDto.getFullName());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        try {
            UserRole role = UserRole.valueOf(registrationDto.getRole().toUpperCase());
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            user.setRole(UserRole.STUDENT);
        }

        if (user.getRole() == UserRole.STUDENT && registrationDto.getDisabilityType() != null) {
            try {
                DisabilityType dt = DisabilityType.valueOf(registrationDto.getDisabilityType().toUpperCase());
                user.setDisabilityType(dt);
            } catch (IllegalArgumentException e) {
                user.setDisabilityType(DisabilityType.OTHER);
            }
        }

        if (user.getRole() == UserRole.STUDENT && registrationDto.getParentEmail() != null && !registrationDto.getParentEmail().isEmpty()) {
            User parent = userRepository.findByEmail(registrationDto.getParentEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Родитель с таким email не найден"));
            if (parent.getRole() != UserRole.PARENT) {
                throw new IllegalArgumentException("Указанный email не принадлежит родителю");
            }
            user.setParent(parent);
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Transactional(readOnly = true)
    public List<User> getChildren(User parent) {
        return userRepository.findByParent(parent);
    }

    @Transactional(readOnly = true)
    public User getChildForParent(User parent, Long childId) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Ребёнок не найден"));
        if (child.getParent() == null || !child.getParent().getId().equals(parent.getId())) {
            throw new SecurityException("Доступ запрещён: это не ваш ребёнок");
        }
        return child;
    }

    @Transactional
    public void updateUserProfile(String email, String fullName, String newPassword, String confirmPassword,
                                  boolean highContrast, boolean subtitles, boolean screenReader,
                                  String fontSize, String colorScheme) {
        User user = findByEmail(email);
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        if (newPassword != null && !newPassword.isEmpty() && newPassword.equals(confirmPassword)) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
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
        userRepository.save(user);
    }

    @Transactional
    public void addChildToParent(String parentEmail, String childEmail) {
        User parent = findByEmail(parentEmail);
        if (parent.getRole() != UserRole.PARENT) {
            throw new IllegalArgumentException("Текущий пользователь не является родителем");
        }
        User child = userRepository.findByEmail(childEmail)
                .orElseThrow(() -> new IllegalArgumentException("Ученик с таким email не найден"));
        if (child.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Указанный пользователь не является учеником");
        }
        if (child.getParent() != null) {
            throw new IllegalArgumentException("Этот ученик уже привязан к другому родителю");
        }
        child.setParent(parent);
        userRepository.save(child);
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }
}