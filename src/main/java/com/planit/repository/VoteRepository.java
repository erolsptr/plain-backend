package com.planit.repository;

import com.planit.model.Task;
import com.planit.model.User;
import com.planit.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Vote'un ID'si Long tipinde olduğu için JpaRepository<Vote, Long> kullanıyoruz.
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // Spring Data JPA, bu metot isminden, User ve Task nesnelerine göre
    // ilgili Vote kaydını bulan bir sorgu oluşturur.
    Optional<Vote> findByUserAndTask(User user, Task task);

}