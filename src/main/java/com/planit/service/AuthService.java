package com.planit.service;

import com.planit.model.User;
import com.planit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.planit.model.dto.LoginRequest;
// Bu record'lar, bu dosya içinde kullanılan basit veri taşıyıcılardır.
// Gelen login isteğinin yapısını tanımlar.

// Frontend'e dönecek olan cevabın yapısını tanımlar.
record AuthResponse(String token, User user) {}


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public User registerUser(User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return null; 
        }

        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }

    public AuthResponse loginUser(LoginRequest loginRequest) {
        // 1. Spring Security'e kullanıcının kimliğini doğrulamasını söyle.
        // Bu, arka planda şifreleri karşılaştırır. Yanlışsa exception fırlatır.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        // 2. Kimlik doğrulama başarılıysa, veritabanından kullanıcıyı bul.
        // E-postanın var olduğundan eminiz çünkü authenticate() başarılı oldu.
        var user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı - bu bir hatadır."));
        
        // 3. Kullanıcı için bir JWT oluştur.
        // Spring Security'nin UserDetails nesnesini değil, doğrudan user nesnemizi kullanabiliriz.
        // JwtService'i buna göre uyarlamamız gerekebilir. Şimdilik bu şekilde bırakalım.
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal(); // Bu da bir yöntem
        String jwtToken = jwtService.generateToken(user.getEmail());
        
        // 4. Şifreyi cevaptan çıkarıp token ve kullanıcı bilgisini döndür.
        user.setPassword(null);
        return new AuthResponse(jwtToken, user);
    }
}