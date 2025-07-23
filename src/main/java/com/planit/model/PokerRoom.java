package com.planit.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "poker_rooms")
@Data
@NoArgsConstructor
public class PokerRoom {

    @Id
    @Column(length = 6)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude // Lombok'un sonsuz döngüye girmesini engeller
    @EqualsAndHashCode.Exclude // Lombok'un sonsuz döngüye girmesini engeller
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "room_participants",
            joinColumns = @JoinColumn(name = "poker_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> participants = new HashSet<>();

    @OneToMany(
            mappedBy = "pokerRoom",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Task> tasks = new ArrayList<>();

    // --- Helper Metotlar ---
    public void addTask(Task task) {
        this.tasks.add(task);
        task.setPokerRoom(this);
    }

    public void addParticipant(User user) {
        this.participants.add(user);
    }
}