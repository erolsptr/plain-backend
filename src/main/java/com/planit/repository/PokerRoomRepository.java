package com.planit.repository;

import com.planit.model.PokerRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

// PokerRoom'un ID'si String tipinde olduğu için JpaRepository<PokerRoom, String> kullanıyoruz.
@Repository
public interface PokerRoomRepository extends JpaRepository<PokerRoom, String> {

    // Bir kullanıcının e-postasına göre,
    // o kullanıcının katılımcı olduğu tüm odaları bulan özel bir sorgu.
    // Bu, "Geçmiş Odalar" listesi için kullanılacak.
    @Query("SELECT pr FROM PokerRoom pr JOIN pr.participants p WHERE p.email = :email")
    Set<PokerRoom> findRoomsByParticipantEmail(String email);
    
}