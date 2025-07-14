package com.planit.model;

import java.util.Set;

// Bu class, bir odaya yeni katılan kullanıcıya gönderilecek ilk veri paketidir.
public class RoomState {
    private Set<String> participants;
    private Task task;

    public RoomState(Set<String> participants, Task task) {
        this.participants = participants;
        this.task = task;
    }

    // Getter'lar
    public Set<String> getParticipants() { return participants; }
    public Task getTask() { return task; }
}