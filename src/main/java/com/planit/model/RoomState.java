package com.planit.model;

import java.util.Map;
import java.util.Set;

public class RoomState {

    private String owner;
    private Set<String> participants;
    private Task activeTask;
    private Map<String, String> votes;
    private boolean areVotesRevealed;

    // Hatanın ana kaynağı: new RoomState() çağrısının çalışması için
    // bu boş constructor'a ihtiyaç var.
    public RoomState() {
    }

    // İkinci kaynak: PokerController'daki tüm "set" metodları burada tanımlı olmalı.
    
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }

    public Task getActiveTask() {
        return activeTask;
    }

    public void setActiveTask(Task activeTask) {
        this.activeTask = activeTask;
    }

    public Map<String, String> getVotes() {
        return votes;
    }

    public void setVotes(Map<String, String> votes) {
        this.votes = votes;
    }

    public boolean getAreVotesRevealed() {
        return areVotesRevealed;
    }

    public void setAreVotesRevealed(boolean areVotesRevealed) {
        this.areVotesRevealed = areVotesRevealed;
    }
}