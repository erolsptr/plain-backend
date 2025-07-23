package com.planit.repository;

import com.planit.model.PokerRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// PokerRoom'un ID'si String tipinde olduğu için JpaRepository<PokerRoom, String> kullanıyoruz.
@Repository
public interface PokerRoomRepository extends JpaRepository<PokerRoom, String> {

    // Spring Data JPA, ileride ihtiyaç duyarsak diye,
    // bir kullanıcının sahip olduğu veya katıldığı odaları bulmak için
    // buraya yazacağımız metot isimlerinden sorgular üretebilir.
    // Örn: List<PokerRoom> findByOwnerId(Long ownerId);
}