package com.planit.controller;

import com.planit.model.Message;
import com.planit.model.RoomState;
import com.planit.model.Task;
import com.planit.model.dto.TaskCreationRequest;
import com.planit.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List; 
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
    
    @DeleteMapping("/api/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId, Authentication authentication) {
        String userEmail = authentication.getName();
        roomService.deleteRoom(roomId, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/rooms/{roomId}/save-result")
    public ResponseEntity<Void> saveVotingResult(@PathVariable String roomId, Authentication authentication) {
        String userEmail = authentication.getName();
        roomService.saveCurrentVotingResult(roomId, userEmail);
        publishFullRoomState(roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/history-updated", "update");
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/api/rooms/{roomId}/tasks")
    public ResponseEntity<List<Map<String, Object>>> getTaskHistory(@PathVariable String roomId, Authentication authentication) {
        String userEmail = authentication.getName();
        List<Map<String, Object>> history = roomService.getTaskHistoryForRoom(roomId, userEmail);
        return ResponseEntity.ok(history);
    }
    
    @PostMapping("/api/rooms/{roomId}/tasks")
    public ResponseEntity<Task> createTaskInRoom(
            @PathVariable String roomId,
            @RequestBody TaskCreationRequest taskRequest,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Task createdTask = roomService.createTask(roomId, taskRequest, userEmail);
        
        // YENİ GÖREV OLUŞTURULDUĞUNDA, HERKESE LİSTELERİNİ YENİLEMESİ İÇİN SİNYAL GÖNDER
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/history-updated", "update");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    // --- YENİ EKLENEN ENDPOINT: HAZIRDAKİ GÖREVLERİ GETİRME ---
    @GetMapping("/api/rooms/{roomId}/pending-tasks")
    public ResponseEntity<List<Task>> getPendingTasks(@PathVariable String roomId, Authentication authentication) {
        String userEmail = authentication.getName();
        List<Task> pendingTasks = roomService.getPendingTasksForRoom(roomId, userEmail);
        return ResponseEntity.ok(pendingTasks);
    }
    // --- YENİ ENDPOINT SONU ---

    // --- WebSocket Mesaj Eşlemeleri ---
    @MessageMapping("/room/{roomId}/register")
    public void register(@DestinationVariable String roomId, @Payload Message joinMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = joinMessage.getSender();
        roomService.addUserToRoom(roomId, username);
        headerAccessor.getSessionAttributes().put("username", username);
        publishFullRoomState(roomId);
    }

    @MessageMapping("/room/{roomId}/set-task")
    public void setTask(@DestinationVariable String roomId, @Payload Task task, SimpMessageHeaderAccessor headerAccessor) {
        // Payload'dan gönderenin adını almak yerine, daha güvenli bir yöntemle
        // WebSocket session'ından alıyoruz.
        String requesterName = (String) headerAccessor.getSessionAttributes().get("username");
        
        String ownerName = roomService.getRoomOwner(roomId);
        if (ownerName == null || !ownerName.equals(requesterName)) {
            return;
        }
        
        // Artık yeni bir Task nesnesi oluşturmuyoruz. Gelenin kendisini kullanıyoruz.
        roomService.setActiveTask(roomId, task); 
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
    
    @MessageMapping("/room/{roomId}/new-round")
    public void newRound(@DestinationVariable String roomId, @Payload Message newRoundMessage) {
        String requester = newRoundMessage.getSender();
        String owner = roomService.getRoomOwner(roomId);
        if (owner == null || !owner.equals(requester)) {
            return;
        }
        roomService.resetLatestTaskVotes(roomId);
        publishFullRoomState(roomId);
    }

    private void publishFullRoomState(String roomId) {
        RoomState currentRoomState = new RoomState();
        currentRoomState.setOwner(roomService.getRoomOwner(roomId));
        currentRoomState.setParticipants(roomService.getUsersInRoom(roomId));
        currentRoomState.setActiveTask(roomService.getActiveTask(roomId));
        currentRoomState.setVotes(roomService.getVotes(roomId) != null ? roomService.getVotes(roomId) : Collections.emptyMap());
        currentRoomState.setAreVotesRevealed(false);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/state", currentRoomState);
    }
}