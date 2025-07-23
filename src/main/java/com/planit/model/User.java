package com.planit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter // Sadece getter metotlarını oluştur
@Setter // Sadece setter metotlarını oluştur
@NoArgsConstructor // Parametresiz (boş) constructor'ı oluştur
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    private Set<PokerRoom> ownedRooms = new HashSet<>();

    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private Set<PokerRoom> joinedRooms = new HashSet<>();


    // --- UserDetails METOTLARI ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    // Bu @Transient'lar UserDetails metotları için gerekli değil,
    // çünkü bu metotların başında "get" veya "is" olduğu için JPA bunları sütun olarak algılamaz.
    // Kodu temiz tutmak için kaldırabiliriz.
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // Lombok'un @Data'sının neden olduğu sonsuz döngüyü önlemek için
    // hashCode ve equals'ı manuel olarak, sadece ID'ye göre tanımlıyoruz.
    // Bu, JPA ilişkilerinde en güvenli yöntemdir.
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null ? id.equals(user.id) : user.id == null;
    }
}