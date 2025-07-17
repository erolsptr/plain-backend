package com.planit.controller;

import com.planit.model.User;
import com.planit.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.planit.model.dto.LoginRequest;
// AuthService'deki record'ları buraya taşıyoruz ki her iki class da kullanabilsin.
// Veya bu record'ları kendi ayrı dosyalarına da koyabiliriz.

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = authService.registerUser(user);
            if (registeredUser == null) {
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }
            registeredUser.setPassword(null);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Invalid user data provided.");
        }
    }

    // YENİ: Login endpoint'i eklendi.
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // AuthService'deki login metodunu çağır ve cevabı direkt döndür.
            // Bu cevap, içinde token ve user bilgisi olan AuthResponse nesnesidir.
            return ResponseEntity.ok(authService.loginUser(loginRequest));
        } catch (Exception e) {
            // AuthenticationManager, kimlik doğrulama başarısız olursa bir exception fırlatır.
            // Bu durumu yakalayıp 401 Unauthorized (Yetkisiz) cevabı dönüyoruz.
            return ResponseEntity.status(401).body("Error: Invalid credentials.");
        }
    }
}