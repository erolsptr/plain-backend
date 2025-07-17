package com.planit.config;

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

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Şifreleri hash'lemek için endüstri standardı olan BCrypt algoritmasını kullanıyoruz.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Stateless (durumsuz) bir API için CSRF korumasını devre dışı bırakıyoruz.
            .csrf(csrf -> csrf.disable())
            // Gelen isteklere yetkilendirme kuralları uyguluyoruz.
            .authorizeHttpRequests(auth -> auth
                // Kayıt, giriş, oda oluşturma ve WebSocket bağlantılarına herkesin erişmesine izin ver.
                .requestMatchers("/api/auth/**", "/api/rooms", "/ws-poker/**").permitAll()
                // Geri kalan tüm istekler için kimlik doğrulaması (login) gerektir.
                .anyRequest().authenticated()
            );
            // Not: JWT konfigürasyonunu daha sonra buraya ekleyeceğiz.
            // Şimdilik temel yol izinlerini ayarlıyoruz.

        return http.build();
    }
}