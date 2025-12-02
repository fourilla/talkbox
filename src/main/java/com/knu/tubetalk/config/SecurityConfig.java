package com.knu.tubetalk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/main").permitAll()
                        .requestMatchers("/search/**").permitAll()
                        .requestMatchers("/thread/**", "/guestbook/**").permitAll()
                        .requestMatchers("/api/comments/thread/**", "/api/comments/guestbook/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/join").permitAll()
                        .requestMatchers("/login", "/join").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            String redirectUrl = request.getHeader("Referer");
                            if (redirectUrl == null || redirectUrl.contains("/login")) {
                                redirectUrl = "/main"; // 예외 대비 기본값
                            }
                            response.sendRedirect(redirectUrl);
                        })
//                        .defaultSuccessUrl("/main", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String redirectUrl = request.getHeader("Referer");
                            if (redirectUrl == null || redirectUrl.contains("/logout")) {
                                redirectUrl = "/main"; // 예외 대비
                            }
                            response.sendRedirect(redirectUrl);
                        })
//                        .logoutSuccessUrl("/login")
                );

        return http.build();
    }
}

