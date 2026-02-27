package com.codearena.service;

import com.codearena.model.Match;
import com.codearena.model.MatchRoom;
import com.codearena.model.User;
import com.codearena.repository.MatchRepository;
import com.codearena.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final Map<String, MatchRoom> activeRooms = new ConcurrentHashMap<>();

    public MatchService(MatchRepository matchRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    public MatchRoom createRoom(String username) {
        User p1 = userRepository.findByUsername(username).orElseThrow();
        String roomId = UUID.randomUUID().toString().substring(0, 6);
        MatchRoom room = new MatchRoom(roomId);
        room.setPlayer1(p1);
        activeRooms.put(roomId, room);
        return room;
    }

    public MatchRoom joinRoom(String roomId, String username) {
        MatchRoom room = activeRooms.get(roomId);
        if (room == null)
            return null;

        if (room.getPlayer1() != null && room.getPlayer1().getUsername().equals(username)) {
            return room;
        }

        if (room.getPlayer2() == null) {
            User p2 = userRepository.findByUsername(username).orElseThrow();
            room.setPlayer2(p2);
            room.setStatus("BAN_PHASE");
            return room;
        } else if (room.getPlayer2().getUsername().equals(username)) {
            return room;
        }

        return null; // Full or not found
    }

    public MatchRoom banLanguage(String roomId, String language) {
        MatchRoom room = activeRooms.get(roomId);
        if (room != null && room.getStatus().equals("BAN_PHASE")) {
            room.getBannedLanguages().add(language); // e.g. "python", "java"
            room.setStatus("CODING");
            room.setStartTime(System.currentTimeMillis());
            return room;
        }
        return null;
    }

    public MatchRoom handleSubmission(String roomId, String username, boolean isAccepted) {
        MatchRoom room = activeRooms.get(roomId);
        if (room != null && room.getStatus().equals("CODING")) {
            if (isAccepted) {
                room.setStatus("FINISHED");
                User winner = room.getPlayer1().getUsername().equals(username) ? room.getPlayer1() : room.getPlayer2();
                User loser = room.getPlayer1().getUsername().equals(username) ? room.getPlayer2() : room.getPlayer1();
                room.setWinner(winner);

                // ELO Calculation (Chess Logic)
                int K = 32;
                double expectedWin = 1.0 / (1.0 + Math.pow(10, (loser.getEloRating() - winner.getEloRating()) / 400.0));

                int newWinnerElo = (int) (winner.getEloRating() + K * (1.0 - expectedWin));
                int newLoserElo = (int) (loser.getEloRating() + K * (0 - (1.0 - expectedWin)));

                winner.setEloRating(newWinnerElo);
                loser.setEloRating(newLoserElo);

                userRepository.save(winner);
                userRepository.save(loser);

                // Save Match History
                Match match = new Match();
                match.setPlayer1(room.getPlayer1());
                match.setPlayer2(room.getPlayer2());
                match.setWinner(winner);
                match.setStartTime(
                        LocalDateTime.now().minus(Duration.ofMillis(System.currentTimeMillis() - room.getStartTime())));
                match.setEndTime(LocalDateTime.now());
                match.setStatus("FINISHED");
                if (!room.getBannedLanguages().isEmpty()) {
                    match.setBannedLanguage(String.join(",", room.getBannedLanguages()));
                }
                matchRepository.save(match);

                // Keep the room alive briefly to broadcast FINISHED state, then could remove
                // activeRooms.remove(roomId);
            }
        }
        return room;
    }

    public MatchRoom getRoom(String roomId) {
        return activeRooms.get(roomId);
    }
}
