package com.planit.service;

import com.planit.model.Task;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    private final Map<String, Task> activeTasks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> roomVotes = new ConcurrentHashMap<>();
    private final Map<String, String> roomOwners = new ConcurrentHashMap<>();

    public void addUserToRoom(String roomId, String username) {
        rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(username);
    }

    public void removeUserFromRoom(String roomId, String username) {
        if (rooms.containsKey(roomId)) {
            rooms.get(roomId).remove(username);
            if (rooms.get(roomId).isEmpty()) {
                rooms.remove(roomId);
            }
        }
    }

    public Set<String> getUsersInRoom(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }

    public void setActiveTask(String roomId, Task task) {
        activeTasks.put(roomId, task);
    }

    public Task getActiveTask(String roomId) {
        return activeTasks.get(roomId);
    }

    public void createRoom(String roomId, String ownerName) {
        Set<String> participants = new HashSet<>();
        participants.add(ownerName);
        rooms.put(roomId, participants);
        roomOwners.put(roomId, ownerName);
    }

    public void recordVote(String roomId, String username, String vote) {
        roomVotes.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(username, vote);
    }

    public Map<String, String> getVotes(String roomId) {
        return roomVotes.get(roomId);
    }

    public void clearVotes(String roomId) {
        if (roomVotes.containsKey(roomId)) {
            roomVotes.get(roomId).clear();
        }
    }

    public String getRoomOwner(String roomId) {
        return roomOwners.get(roomId);
    }
}