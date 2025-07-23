package com.planit.repository;

import com.planit.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Task'ın ID'si Long tipinde olduğu için JpaRepository<Task, Long> kullanıyoruz.
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
}