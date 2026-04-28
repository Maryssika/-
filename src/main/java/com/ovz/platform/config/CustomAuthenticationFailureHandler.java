package com.ovz.platform.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import java.io.IOException;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String selectedRole = request.getParameter("selectedRole");
        String errorMsg = "Неверный email или пароль";

        // Если пришла выбранная роль, добавим её в параметры для отображения в форме
        if (selectedRole != null && !selectedRole.isEmpty()) {
            response.sendRedirect("/login?error=true&role=" + selectedRole);
        } else {
            response.sendRedirect("/login?error=true");
        }
    }
}