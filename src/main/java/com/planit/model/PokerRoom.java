package com.planit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "poker_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokerRoom {

    @Id
    @Column(length = 6) // Oda ID'miz 6 karakterliydi
    private String id;

    // --- İLİŞKİLER ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "room_participants",
            joinColumns = @JoinColumn(name = "poker_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @OneToMany(
            mappedBy = "pokerRoom",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Task> tasks = new ArrayList<>();

    // Helper metotlar (ilişkileri senkronize tutmak için)
    public void addTask(Task task) {
        tasks.add(task);
        task.setPokerRoom(this);
    }

    public void addParticipant(User user) {
        participants.add(user);
    }
}