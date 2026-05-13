package com.ovz.platform.config;

import com.ovz.platform.models.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String selectedRole = request.getParameter("selectedRole");
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Получаем реальную роль пользователя
        String userRole = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("")
                .replace("ROLE_", "");

        // ЕСЛИ ПОЛЬЗОВАТЕЛЬ — АДМИНИСТРАТОР, ТО ПРОВЕРКУ ПРОПУСКАЕМ
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            // Для не-админов проверяем соответствие выбранной роли
            if (selectedRole != null && !selectedRole.isEmpty() && !selectedRole.equalsIgnoreCase(userRole)) {
                response.sendRedirect("/login?error=role_mismatch&role=" + selectedRole);
                return;
            }
        }

        // Определяем URL редиректа (оставляем как было)
        String redirectUrl = "/profile";
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_" + UserRole.ADMIN.name())) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (role.equals("ROLE_" + UserRole.TEACHER.name())) {
                redirectUrl = "/teacher/dashboard";
                break;
            } else if (role.equals("ROLE_" + UserRole.STUDENT.name())) {
                redirectUrl = "/student/dashboard";
                break;
            } else if (role.equals("ROLE_" + UserRole.PARENT.name())) {
                redirectUrl = "/parent/dashboard";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}