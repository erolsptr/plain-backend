package com.planit.service;

import com.planit.model.User;
import com.planit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User newUser) {
        // E-postanın zaten kullanımda olup olmadığını kontrol et
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            // Gerçek bir uygulamada burada özel bir exception fırlatmak daha iyidir.
            // Şimdilik null döndürelim, Controller'da bunu kontrol edeceğiz.
            return null; 
        }

        // Şifreyi veritabanına kaydetmeden önce güvenli bir şekilde hash'le
        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);

        // Yeni kullanıcıyı veritabanına kaydet
        return userRepository.save(newUser);
    }

    // Login (kullanıcı girişi) mantığını daha sonra,
    // Spring Security ve JWT'yi yapılandırdığımızda buraya ekleyeceğiz.
}