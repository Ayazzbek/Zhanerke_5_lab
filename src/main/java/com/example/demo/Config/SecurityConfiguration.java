package com.example.demo.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;

    public SecurityConfiguration(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/user/register", "/user/login", "/images/**").permitAll() // Разрешаем регистрацию, логин и доступ к статическим ресурсам
                        .requestMatchers("/user/list", "/categories/**").hasRole("ADMIN") // Доступ к админским страницам только для ADMIN
                        .requestMatchers("/tasks/add", "/tasks/edit/**", "/tasks/delete/**").hasRole("ADMIN")
                        .requestMatchers("/tasks/**").hasAnyRole("USER", "ADMIN") // Пользователи и админы могут работать с задачами
                        .requestMatchers("/user/profile", "/user/profile/edit", "/user/profile/update").hasAnyRole("USER", "ADMIN") // Профиль доступен для USER и ADMIN
                        .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
                )
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/", true) // Перенаправление после успешного входа
                        .permitAll() // Разрешаем доступ ко всем
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/user/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/user/register", "/user/login","/categories/save","/error") // Игнорируем CSRF для этих URL
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error")
                );
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}


