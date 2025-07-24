package com.planit.controller;

import com.planit.model.Message;
import com.planit.model.RoomState;
import com.planit.model.Task;
import com.planit.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
public class PokerController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/api/rooms")
    public ResponseEntity<Map<String, String>> createRoom(Authentication authentication) {
        String ownerEmail = authentication.getName(); 
        String newRoomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        roomService.createRoom(newRoomId, ownerEmail); 
        return ResponseEntity.ok(Map.of("roomId", newRoomId));
    }

    @GetMapping("/api/rooms")
    public ResponseEntity<Set<Map<String, String>>> getUserRooms(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(roomService.findRoomsByUserEmail(userEmail));
    }

    @MessageMapping("/room/{roomId}/register")
    public void register(@DestinationVariable String roomId, @Payload Message joinMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = joinMessage.getSender();
        roomService.addUserToRoom(roomId, username);
        headerAccessor.getSessionAttributes().put("username", username);
        publishFullRoomState(roomId);
    }

    @MessageMapping("/room/{roomId}/set-task")
    public void setTask(@DestinationVariable String roomId, @Payload Message taskMessage) {
        String requester = taskMessage.getSender();
        String owner = roomService.getRoomOwner(roomId);
        if (owner == null || !owner.equals(requester)) {
            return;
        }
        Task newTask = new Task();
        newTask.setTitle(taskMessage.getContent());
        newTask.setDescription(taskMessage.getDescription());
        newTask.setCardSet(taskMessage.getCardSet());
        
        roomService.setActiveTask(roomId, newTask);
        roomService.clearVotes(roomId);
        publishFullRoomState(roomId);
    }

    @MessageMapping("/room/{roomId}/vote")
    public void vote(@DestinationVariable String roomId, @Payload Message voteMessage) {
        String username = voteMessage.getSender();
        String voteValue = voteMessage.getContent();
        roomService.recordVote(roomId, username, voteValue);
        Map<String, String> currentVotes = roomService.getVotes(roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/votes", currentVotes);
    }

    @MessageMapping("/room/{roomId}/reveal")
    public void revealVotes(@DestinationVariable String roomId, @Payload Message revealMessage) {
        String requester = revealMessage.getSender();
        String owner = roomService.getRoomOwner(roomId);
        if (owner == null || !owner.equals(requester)) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/reveal", Map.of("reveal", true));
    }
    
    // YENİ: Sadece "Yeni Tur Başlat" için endpoint
    @MessageMapping("/room/{roomId}/new-round")
    public void newRound(@DestinationVariable String roomId, @Payload Message newRoundMessage) {
        // Yetki kontrolü
        String requester = newRoundMessage.getSender();
        String owner = roomService.getRoomOwner(roomId);
        if (owner == null || !owner.equals(requester)) {
            return;
        }

        // Sadece oyları sıfırlayan yeni servis metodunu çağır
        roomService.resetLatestTaskVotes(roomId);

        // Oyları sıfırlanmış güncel durumu herkese gönder
        publishFullRoomState(roomId);
    }

    private void publishFullRoomState(String roomId) {
        // Bu helper metot, birden fazla yerden çağrıldığı için çok faydalı ve korunuyor.
        RoomState currentRoomState = new RoomState();
        currentRoomState.setOwner(roomService.getRoomOwner(roomId));
        currentRoomState.setParticipants(roomService.getUsersInRoom(roomId));
        currentRoomState.setActiveTask(roomService.getActiveTask(roomId));
        currentRoomState.setVotes(roomService.getVotes(roomId) != null ? roomService.getVotes(roomId) : Collections.emptyMap());
        currentRoomState.setAreVotesRevealed(false);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/state", currentRoomState);
    }
}