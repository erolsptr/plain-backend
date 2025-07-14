// Düzeltilmiş PokerController.java - TAM HALİ

package com.planit.controller;

import com.planit.model.Message;
import com.planit.model.Task;
import com.planit.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser; // Bu import önemli!
import org.springframework.stereotype.Controller;

import java.util.Set;

@Controller
public class PokerController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/join")
    public void joinRoom(@DestinationVariable String roomId, @Payload Message chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        roomService.addUserToRoom(roomId, username);
        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        Set<String> usersInRoom = roomService.getUsersInRoom(roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/participants", usersInRoom);
    }

    @MessageMapping("/room/{roomId}/set-task")
    public void setTask(@DestinationVariable String roomId, @Payload Task task) {
        roomService.setActiveTask(roomId, task);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/task", task);
    }

    // YENİ ve BASİTLEŞTİRİLMİŞ getTask metodu
    @MessageMapping("/room/{roomId}/get-task")
    @SendToUser("/queue/task")
    public Task getTask(@DestinationVariable String roomId) {
        return roomService.getActiveTask(roomId);
    }

    @MessageMapping("/room/{roomId}/vote")
    public void vote(@DestinationVariable String roomId, @Payload Message voteMessage) {
        String message = voteMessage.getSender() + " oy verdi: " + voteMessage.getContent();
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/votes", message);
    }
}