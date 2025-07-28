package com.planit.repository;

import com.planit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA, metot isminden sorguyu otomatik olarak oluşturur.
    // "SELECT * FROM users WHERE email = ?" sorgusunu çalıştırır.
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
}