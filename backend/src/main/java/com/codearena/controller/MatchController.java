package com.codearena.controller;

import com.codearena.model.MatchRoom;
import com.codearena.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin("*")
public class MatchController {

    private final MatchService matchService;
    private final SimpMessagingTemplate messagingTemplate;

    public MatchController(MatchService matchService, SimpMessagingTemplate messagingTemplate) {
        this.matchService = matchService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/api/match/create")
    public ResponseEntity<MatchRoom> createRoom(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        MatchRoom room = matchService.createRoom(username);
        return ResponseEntity.ok(room);
    }

    @MessageMapping("/match/{roomId}/join")
    public void joinRoom(@DestinationVariable String roomId, @Payload Map<String, String> payload) {
        String username = payload.get("username");
        MatchRoom room = matchService.joinRoom(roomId, username);
        if (room != null) {
            messagingTemplate.convertAndSend("/topic/match/" + roomId, room);
        }
    }

    @MessageMapping("/match/{roomId}/ban")
    public void banLanguage(@DestinationVariable String roomId, @Payload Map<String, String> payload) {
        // e.g. language: "python"
        String language = payload.get("language");
        MatchRoom room = matchService.banLanguage(roomId, language);
        if (room != null) {
            messagingTemplate.convertAndSend("/topic/match/" + roomId, room);
        }
    }

    @MessageMapping("/match/{roomId}/submit")
    public void submitCode(@DestinationVariable String roomId, @Payload Map<String, Object> payload) {
        String username = (String) payload.get("username");
        Boolean isAccepted = (Boolean) payload.get("isAccepted"); // Mocking judging success for module 4 fast test

        // Chess/Timer penalty logic can also be added here by broadcasting a "penalty"
        // event
        if (!isAccepted) {
            messagingTemplate.convertAndSend("/topic/match/" + roomId + "/penalty",
                    Map.of("username", username, "penaltySeconds", 120));
        }

        MatchRoom room = matchService.handleSubmission(roomId, username, isAccepted);
        if (room != null) {
            messagingTemplate.convertAndSend("/topic/match/" + roomId, room);
        }
    }
}
