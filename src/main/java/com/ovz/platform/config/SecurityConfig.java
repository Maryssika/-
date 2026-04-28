package com.ovz.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/", "/about", "/features", "/demo/**",
                                "/login", "/register", "/registration-success",
                                "/learning", "/team", "/contacts", "/start-learning",
                                "/css/**", "/js/**", "/images/**", "/h2-console/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .requestMatchers("/parent/**").hasRole("PARENT")
                        .requestMatchers("/settings", "/settings/**", "/notifications").authenticated()
                        .requestMatchers("/forgot-password", "/reset-password").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureHandler(customAuthenticationFailureHandler())   // новый обработчик
                        .failureUrl("/login?error=true")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKeyForOVZPlatform")
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 дней
                        .rememberMeParameter("remember-me")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Для H2 Console
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/login", "/register")
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
}