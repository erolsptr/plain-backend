package com.planit.service;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.planit.model.Task;

@Service
public class RoomService {
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    // YENİ: Her odanın aktif görevini tutacak map.
    // Key: Oda ID'si, Value: Aktif Görev nesnesi
    private final Map<String, Task> activeTasks = new ConcurrentHashMap<>();

    public void addUserToRoom(String roomId, String username) {
        // Eğer oda yoksa, yeni bir Set (katılımcı listesi) ile oluştur.
        // Varsa, mevcut listeye kullanıcıyı ekle.
        rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(username);
    }

    public void removeUserFromRoom(String roomId, String username) {
        if (rooms.containsKey(roomId)) {
            rooms.get(roomId).remove(username);
            // Eğer oda boş kalırsa, hafızadan silelim.
            if (rooms.get(roomId).isEmpty()) {
                rooms.remove(roomId);
            }
        }
    }

    public Set<String> getUsersInRoom(String roomId) {
        // Eğer oda yoksa, boş bir liste döndür.
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }

    public void setActiveTask(String roomId, Task task) {
    activeTasks.put(roomId, task);
    }

    public Task getActiveTask(String roomId) {
        return activeTasks.get(roomId);
    }
    
}