package com.planit.service;

import com.planit.model.PokerRoom;
import com.planit.model.Task;
import com.planit.model.User;
import com.planit.model.Vote;
import com.planit.model.dto.TaskCreationRequest;
import com.planit.repository.PokerRoomRepository;
import com.planit.repository.TaskRepository;
import com.planit.repository.UserRepository;
import com.planit.repository.VoteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final PokerRoomRepository pokerRoomRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final VoteRepository voteRepository; 

    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    private final Map<String, Task> activeTasks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> roomVotes = new ConcurrentHashMap<>();
    private final Map<String, String> roomOwners = new ConcurrentHashMap<>();

    @Transactional
    public void addUserToRoom(String roomId, String username) {
        rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(username);
        PokerRoom room = pokerRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı eklenecek oda bulunamadı: " + roomId));
        User userToJoin = userRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException("Odaya katılacak kullanıcı bulunamadı: " + username));
        room.addParticipant(userToJoin);
    }
    
    public void removeUserFromRoom(String roomId, String username) { /* ... aynı ... */ }
    public Set<String> getUsersInRoom(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }
    public Task getActiveTask(String roomId) {
        return activeTasks.get(roomId);
    }
    public void recordVote(String roomId, String username, String vote) {
        roomVotes.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(username.trim(), vote);
    }
    public Map<String, String> getVotes(String roomId) {
        return roomVotes.get(roomId);
    }
    public void clearVotes(String roomId) {
        if (roomVotes.containsKey(roomId)) {
            roomVotes.get(roomId).clear();
        }
    }
    
    @Transactional
    public String getRoomOwner(String roomId) {
        String ownerName = roomOwners.get(roomId);
        if (ownerName != null) { return ownerName; }
        PokerRoom room = pokerRoomRepository.findById(roomId).orElse(null);
        if (room != null) {
            ownerName = room.getOwner().getName();
            roomOwners.put(roomId, ownerName);
            return ownerName;
        }
        return null;
    }

    @Transactional
    public List<Map<String, Object>> getTaskHistoryForRoom(String roomId, String requesterEmail) {
        PokerRoom room = pokerRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Oda bulunamadı: " + roomId));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + requesterEmail));
        boolean isParticipant = room.getParticipants().stream().anyMatch(p -> p.getId().equals(requester.getId()));
        if (!isParticipant) { throw new AccessDeniedException("Bu odanın geçmişini görme yetkiniz yok."); }
        List<Task> tasks = room.getTasks();
        return tasks.stream().filter(task -> task.getVotes() != null && !task.getVotes().isEmpty()).map(task -> {
            Map<String, Long> voteCounts = task.getVotes().stream().collect(Collectors.groupingBy(Vote::getVoteValue, Collectors.counting()));
            String consensusScore = voteCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
            Long maxVoteId = task.getVotes().stream().map(Vote::getId).max(Comparator.naturalOrder()).orElse(0L);
            Map<String, String> votesMap = task.getVotes().stream().collect(Collectors.toMap(vote -> vote.getUser().getName(), Vote::getVoteValue));
            return Map.of("taskId", (Object)task.getId(), "title", (Object)task.getTitle(), "description", (Object)task.getDescription(), "consensusScore", (Object)consensusScore, "completionOrder", (Object)maxVoteId, "votes", (Object)votesMap);
        }).sorted(Comparator.comparing((Map<String, Object> m) -> (Long)m.get("completionOrder")).reversed()).collect(Collectors.toList());
    }

    @Transactional
    public void saveCurrentVotingResult(String roomId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("İsteği yapan kullanıcı bulunamadı: " + requesterEmail));
        String ownerName = getRoomOwner(roomId);
        if (ownerName == null || !requester.getName().equals(ownerName)) { throw new AccessDeniedException("Sadece oda sahibi sonuçları kaydedebilir."); }
        Task currentTask = getActiveTask(roomId);
        if (currentTask == null || currentTask.getId() == null || getVotes(roomId) == null || getVotes(roomId).isEmpty()) {
            activeTasks.remove(roomId);
            clearVotes(roomId);
            return;
        }
        List<Vote> votesToSave = new ArrayList<>();
        for (Map.Entry<String, String> entry : getVotes(roomId).entrySet()) {
            String userName = entry.getKey();
            String voteValue = entry.getValue();
            User voter = userRepository.findByName(userName).orElseThrow(() -> new RuntimeException("DB'de '" + userName + "' adında kullanıcı bulunamadı."));
            Vote vote = new Vote();
            vote.setUser(voter);
            vote.setVoteValue(voteValue);
            vote.setTask(currentTask);
            votesToSave.add(vote);
        }
        voteRepository.saveAll(votesToSave);
        activeTasks.remove(roomId);
        clearVotes(roomId);
    }
    
    @Transactional
    public void deleteRoom(String roomId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("İsteği yapan kullanıcı bulunamadı: " + requesterEmail));
        PokerRoom roomToDelete = pokerRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Silinecek oda bulunamadı: " + roomId));
        if (!requester.getName().equals(roomToDelete.getOwner().getName())) { throw new AccessDeniedException("Bu odayı silme yetkiniz yok."); }
        pokerRoomRepository.deleteById(roomId);
        rooms.remove(roomId);
        activeTasks.remove(roomId);
        roomVotes.remove(roomId);
        roomOwners.remove(roomId);
    }

    @Transactional
    public void createRoom(String roomId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail).orElseThrow(() -> new RuntimeException("Oda sahibi kullanıcı bulunamadı: " + ownerEmail));
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
    public Task createTask(String roomId, TaskCreationRequest taskRequest, String requesterEmail) {
        PokerRoom room = pokerRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Görev eklenecek oda bulunamadı: " + roomId));
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow(() -> new RuntimeException("İsteği yapan kullanıcı bulunamadı: " + requesterEmail));
        if (!room.getOwner().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Sadece oda sahibi yeni görev oluşturabilir.");
        }
        Task newTask = new Task();
        newTask.setTitle(taskRequest.getTitle());
        newTask.setDescription(taskRequest.getDescription());
        newTask.setCardSet(taskRequest.getCardSet());
        newTask.setPokerRoom(room);
        return taskRepository.save(newTask);
    }

        @Transactional
    public List<Task> getPendingTasksForRoom(String roomId, String requesterEmail) {
        System.out.println("\n--- [1] getPendingTasksForRoom METODU ÇAĞRILDI ---");
        System.out.println("Oda ID: " + roomId);

        // Yetki kontrolü
        PokerRoom room = pokerRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı: " + roomId));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + requesterEmail));
        boolean isParticipant = room.getParticipants().stream().anyMatch(p -> p.getId().equals(requester.getId()));
        if (!isParticipant) {
            throw new AccessDeniedException("Bu odanın görevlerini görme yetkiniz yok.");
        }
        System.out.println("-> [2] Yetki kontrolü BAŞARILI.");

        // Veritabanından tüm görevleri çek
        List<Task> allTasks = taskRepository.findByPokerRoomId(roomId);
        System.out.println("-> [3] Veritabanından bu odaya ait toplam " + allTasks.size() + " adet görev bulundu.");

        // Filtreleme işlemi
        List<Task> pendingTasks = new ArrayList<>();
        System.out.println("-> [4] Filtreleme başlıyor...");
        for (Task task : allTasks) {
            long voteCount = voteRepository.countByTaskId(task.getId());
            System.out.println("   - Görev ID: " + task.getId() + " | Başlık: '" + task.getTitle() + "' | Oy Sayısı: " + voteCount);
            if (voteCount == 0) {
                pendingTasks.add(task);
                System.out.println("     -> BU GÖREV 'HAZIR OLANLAR' LİSTESİNE EKLENDİ.");
            } else {
                System.out.println("     -> BU GÖREV 'HAZIR OLANLAR' LİSTESİNE EKLENMEDİ (çünkü oylanmış).");
            }
        }
        
        System.out.println("-> [5] Filtreleme tamamlandı. Toplam " + pendingTasks.size() + " adet hazır görev döndürülüyor.");
        return pendingTasks;
    }

        // --- BU METOT NİHAİ OLARAK DÜZELTİLDİ ---
    @Transactional
    public void setActiveTask(String roomId, Task task) {
        PokerRoom room = pokerRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Görev eklenecek oda veritabanında bulunamadı: " + roomId));
        
        task.setPokerRoom(room);

        Task taskToActivate;
        // Eğer görevin bir ID'si yoksa (bu artık HİÇBİR ZAMAN olmayacak ama güvenlik için kalsın),
        // bu yeni bir görevdir. Kaydet.
        if (task.getId() == null) {
            taskToActivate = taskRepository.save(task);
        } else {
            // Eğer bir ID'si varsa, bu "Hazır Olanlar" listesinden gelen VAR OLAN bir görevdir.
            // Tekrar kaydetme, doğrudan kendisini kullan.
            taskToActivate = task;
        }
        
        // Hafızaya, her zaman ID'si olan, doğru görevi koy.
        activeTasks.put(roomId, taskToActivate); 
    }
    // --- DÜZELTME SONU ---

    @Transactional
    public Set<Map<String, String>> findRoomsByUserEmail(String userEmail) {
        Set<PokerRoom> userRooms = pokerRoomRepository.findRoomsByParticipantEmail(userEmail);
        return userRooms.stream().map(room -> Map.of( "roomId", room.getId(), "ownerName", room.getOwner().getName(), "taskCount", String.valueOf(room.getTasks().size()) )).collect(Collectors.toSet());
    }

    @Transactional
    public void resetLatestTaskVotes(String roomId) {
        clearVotes(roomId);
        PokerRoom room = pokerRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Oda veritabanında bulunamadı: " + roomId));
        if (!room.getTasks().isEmpty()) {
            Task latestTask = room.getTasks().get(room.getTasks().size() - 1);
            voteRepository.deleteAll(latestTask.getVotes());
        }
    }
}