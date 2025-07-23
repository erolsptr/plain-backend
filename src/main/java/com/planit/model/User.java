package com.planit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @JsonIgnore // Şifreyi asla JSON olarak gönderme
    private String password;

    @Column(nullable = false)
    private String name;

    // --- İLİŞKİLER ---
    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PokerRoom> ownedRooms = new HashSet<>();

    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<PokerRoom> joinedRooms = new HashSet<>();


    // --- UserDetails METOTLARI (Aynı kalıyor) ---
    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() { return true; }

    @Override
    @Transient
    public boolean isAccountNonLocked() { return true; }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    @Transient
    public boolean isEnabled() { return true; }
}