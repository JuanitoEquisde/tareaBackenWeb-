package com.easydates.easydateap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
//jelouuuuuu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ✅ Rutas públicas
                        .requestMatchers(
                                "/login",
                                "/registro",
                                "/login/invitado",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/admin/migrate-passwords"
                        ).permitAll()

                        // ✅ Rutas de cliente (cualquier usuario autenticado)
                        .requestMatchers("/cliente/**").authenticated()

                        // ✅ NUEVO: Rutas de suscripciones (usuarios autenticados)
                        .requestMatchers("/suscripciones/**").authenticated()

                        // ✅ Rutas de admin: solo ADMINISTRADOR
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()

                );

        return http.build();
    }

}