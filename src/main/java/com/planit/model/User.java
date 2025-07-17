package com.planit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data // Lombok: Getter, Setter, toString, equals, hashCode metodlarını otomatik oluşturur.
@NoArgsConstructor // Lombok: Boş bir constructor oluşturur. JPA için gereklidir.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // 'name' alanı, Poker odalarında görünecek olan addır.
    // 'email' ile aynı olabilir ama farklılaşmasına da izin verelim.
    @Column(nullable = false)
    private String name;

}