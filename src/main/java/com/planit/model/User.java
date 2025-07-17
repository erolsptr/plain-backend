package com.planit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    // --- UserDetails METOTLARI ---
    // Bu metotlar, User nesnemizi Spring Security'nin anlayacağı formata sokar.

    @Override
    @Transient // Bu alanın veritabanı ile ilişkili olmadığını belirtir.
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Şimdilik tüm kullanıcılara basit bir 'USER' rolü veriyoruz.
        // İleride rolleri veritabanında saklayabiliriz.
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        // Spring Security için "username", bizim sistemimizdeki "email"dir.
        return this.email;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true; // Hesap süresiz geçerli.
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return true; // Hesap kilitli değil.
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true; // Parola süresiz geçerli.
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true; // Hesap aktif.
    }
}