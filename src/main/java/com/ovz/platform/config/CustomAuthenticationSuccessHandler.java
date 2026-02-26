package com.ovz.platform.config;

import com.ovz.platform.models.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

// УБРАТЬ @Component отсюда!
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/profile"; // По умолчанию

        // Определяем URL в зависимости от роли
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