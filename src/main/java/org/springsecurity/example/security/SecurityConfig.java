package org.springsecurity.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

// Конфигурация безопасности для приложения
@Configuration
// Включает поддержку Spring Security
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return webSecurity -> webSecurity.ignoring().requestMatchers("/h2-console/**");
    }

    // Основной фильтр безопасности. CSRF-токен хранится в HTTP-сессии
    // и добавляется Thymeleaf в POST-форму входа.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(withDefaults())
                .sessionManagement(withDefaults())
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/login", "/403", "/css/**").permitAll()
                                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/company/**").hasAuthority("ROLE_MANAGER")
                                .requestMatchers("/users", "/users/**")
                                    .hasAnyAuthority("ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN")
                                .requestMatchers("/info").permitAll()
                                .requestMatchers("/**").denyAll()
                )
                .formLogin(fl ->
                                fl.loginPage("/login")
                                .loginProcessingUrl("/login")
                                .failureUrl("/login?error")
                                .successHandler((request, response, authentication) -> {
                                    boolean isAdmin = authentication.getAuthorities().stream()
                                            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                                    String targetUrl = isAdmin ? "/admin" : "/users";
                                    response.sendRedirect(request.getContextPath() + targetUrl);
                                })
                )
                .exceptionHandling(exceptions -> exceptions.accessDeniedPage("/403"))
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
