package com.planit.service;

import com.planit.model.PokerRoom;
import com.planit.model.Task;
import com.planit.model.User;
import com.planit.repository.PokerRoomRepository;
import com.planit.repository.TaskRepository;
import com.planit.repository.UserRepository;
import com.planit.repository.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final PokerRoomRepository pokerRoomRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final VoteRepository voteRepository; // Vote'ları veritabanından silmek için eklendi

    // --- MEVCUT WEBSOCKET AKIŞI İÇİN IN-MEMORY YAPI (KORUNUYOR) ---
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    private final Map<String, Task> activeTasks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> roomVotes = new ConcurrentHashMap<>();
    private final Map<String, String> roomOwners = new ConcurrentHashMap<>();

    // --- MEVCUT METOTLAR (DEĞİŞTİRİLMEDİ) ---
    public void addUserToRoom(String roomId, String username) {
        rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(username);
    }
    public void removeUserFromRoom(String roomId, String username) { /* ... aynı ... */ }
    public Set<String> getUsersInRoom(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }
    public Task getActiveTask(String roomId) {
        return activeTasks.get(roomId);
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

    // --- GÜNCELLENMİŞ VE YENİ METOTLAR ---

    @Transactional
    public void createRoom(String roomId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Oda sahibi kullanıcı bulunamadı: " + ownerEmail));
        
        String ownerName = owner.getName();
        Set<String> participants = new HashSet<>();
        participants.add(ownerName);
        rooms.put(roomId, participants);
        roomOwners.put(roomId, ownerName);

        PokerRoom newRoom = new PokerRoom();
        newRoom.setId(roomId);
        newRoom.setOwner(owner);
        newRoom.addParticipant(owner);
        pokerRoomRepository.save(newRoom);
    }

    @Transactional
    public void setActiveTask(String roomId, Task task) {
        activeTasks.put(roomId, task);

        PokerRoom room = pokerRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Görev eklenecek oda veritabanında bulunamadı: " + roomId));
        
        room.getTasks().add(task);
        task.setPokerRoom(room);
        
        pokerRoomRepository.save(room);
    }

    @Transactional
    public Set<Map<String, String>> findRoomsByUserEmail(String userEmail) {
        Set<PokerRoom> userRooms = pokerRoomRepository.findRoomsByParticipantEmail(userEmail);
        
        return userRooms.stream().map(room -> Map.of(
                    "roomId", room.getId(),
                    "ownerName", room.getOwner().getName(),
                    "taskCount", String.valueOf(room.getTasks().size())
                )).collect(Collectors.toSet());
    }

    // YENİ: Sadece oyları sıfırlayan metot
    @Transactional
    public void resetLatestTaskVotes(String roomId) {
        // 1. Önce in-memory oyları temizle
        clearVotes(roomId);
        
        // 2. Sonra veritabanındaki oyları temizle
        PokerRoom room = pokerRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Oda veritabanında bulunamadı: " + roomId));

        if (!room.getTasks().isEmpty()) {
            // Aktif olan son görevi al
            Task latestTask = room.getTasks().get(room.getTasks().size() - 1);
            // Bu göreve bağlı tüm oy kayıtlarını veritabanından sil
            voteRepository.deleteAll(latestTask.getVotes());
        }
    }
}