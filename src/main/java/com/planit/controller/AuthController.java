package com.planit.controller;

import com.planit.model.User;
import com.planit.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User registeredUser = authService.registerUser(user);

        if (registeredUser == null) {
            // AuthService, e-posta zaten varsa null döndürüyordu.
            // Bu durumda bir hata mesajıyla birlikte 400 Bad Request döndürüyoruz.
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Başarılı kayıtta, şifre alanını null yaparak cevaptan çıkarıyoruz.
        // Asla hash'lenmiş şifreyi bile geri göndermeyiz.
        registeredUser.setPassword(null);

        return ResponseEntity.ok(registeredUser);
    }

    // Login endpoint'ini daha sonra buraya ekleyeceğiz.
    // @PostMapping("/login")
    // ...
}