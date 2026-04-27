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

    @Transactional
    public List<User> getStudentsForTeacher(String teacherEmail) {
        User teacher = findByEmail(teacherEmail);
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("Пользователь не является учителем");
        }
        return teacher.getStudents();
    }

    @Transactional
    public void addStudentToTeacher(String teacherEmail, String studentEmail) {
        User teacher = findByEmail(teacherEmail);
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("Учитель не найден");
        }
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("Ученик не найден"));
        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Пользователь не является учеником");
        }
        student.setTeacher(teacher);
        userRepository.save(student);
    }

    @Transactional
    public void removeStudentFromTeacher(String teacherEmail, Long studentId) {
        User teacher = findByEmail(teacherEmail);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Ученик не найден"));
        if (student.getTeacher() == null || !student.getTeacher().getId().equals(teacher.getId())) {
            throw new SecurityException("Этот ученик не привязан к вам");
        }
        student.setTeacher(null);
        userRepository.save(student);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    @Transactional(readOnly = true)
    public long countAllUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void updateUserByAdmin(Long id, UserRegistrationDto dto) {
        User user = findById(id);

        // 1. Обновляем основные поля
        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            user.setFullName(dto.getFullName());
        }

        // 2. Email — с проверкой уникальности
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String newEmail = dto.getEmail().trim();
            if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
            user.setEmail(newEmail);
        }

        // 3. Пароль — меняем, только если введён новый и он прошёл валидацию
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new IllegalArgumentException("Пароли не совпадают");
            }
            if (dto.getPassword().length() < 8) {
                throw new IllegalArgumentException("Пароль должен содержать минимум 8 символов");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // 4. Роль
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            try {
                UserRole role = UserRole.valueOf(dto.getRole().toUpperCase());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Некорректная роль: " + dto.getRole());
            }
        }

        // 5. Тип нарушения (только для STUDENT)
        if (user.getRole() == UserRole.STUDENT) {
            if (dto.getDisabilityType() != null && !dto.getDisabilityType().isEmpty()) {
                try {
                    user.setDisabilityType(DisabilityType.valueOf(dto.getDisabilityType().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    user.setDisabilityType(DisabilityType.OTHER);
                }
            } else {
                user.setDisabilityType(null);
            }
        } else {
            user.setDisabilityType(null);
        }

        // 6. Сохраняем
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        // При необходимости удалить связи (зависит от каскадов)
        // Например, если удаляем учителя, можно отвязать учеников
        if (user.getRole() == UserRole.TEACHER) {
            for (User student : user.getStudents()) {
                student.setTeacher(null);
                userRepository.save(student);
            }
        }
        if (user.getRole() == UserRole.PARENT) {
            for (User child : user.getChildren()) {
                child.setParent(null);
                userRepository.save(child);
            }
        }
        userRepository.delete(user);
    }
}