package com.planit.controller;

import com.planit.model.Message;
import com.planit.model.Task;
import com.planit.model.RoomState; // Bu importu ekle
import com.planit.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
public class PokerController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // --- REST API for Room Creation ---
    @PostMapping("/api/rooms")
    public ResponseEntity<Map<String, String>> createRoom(@RequestBody Map<String, String> payload) {
        String ownerName = payload.get("name");
        if (ownerName == null || ownerName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Owner name is required."));
        }
        String newRoomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        roomService.createRoom(newRoomId, ownerName);
        return ResponseEntity.ok(Map.of("roomId", newRoomId));
    }

    // --- WebSocket Endpoints ---

    /**
     * Odaya yeni bir kullanıcı katıldığında bu metot tetiklenir.
     * Hem güncel katılımcı listesini hem de (varsa) güncel görevi
     * HERKESE AÇIK kanallara yeniden yayınlar.
     * Bu sayede yeni katılan kullanıcı da odanın durumunu öğrenir.
     */
    @MessageMapping("/room/{roomId}/register")
    public void register(@DestinationVariable String roomId, @Payload Message joinMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = joinMessage.getSender();
        roomService.addUserToRoom(roomId, username);
        headerAccessor.getSessionAttributes().put("username", username);

        // 1. Güncel katılımcı listesini HERKESE gönder.
        Set<String> updatedParticipants = roomService.getUsersInRoom(roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/participants", updatedParticipants);

        // 2. Mevcut görevi (eğer varsa) HERKESE gönder.
        Task currentTask = roomService.getActiveTask(roomId);
        if (currentTask != null) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/task", currentTask);
        }
    }

    @MessageMapping("/room/{roomId}/set-task")
    public void setTask(@DestinationVariable String roomId, @Payload Task task) {
        roomService.setActiveTask(roomId, task);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/task", task);
    }

    @MessageMapping("/room/{roomId}/vote")
    public void vote(@DestinationVariable String roomId, @Payload Message voteMessage) {
        // ... oylama mantığı
    }
}