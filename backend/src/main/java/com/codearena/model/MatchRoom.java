package com.codearena.model;

import java.util.ArrayList;
import java.util.List;

public class MatchRoom {
    private String roomId;
    private User player1;
    private User player2;
    private String status; // WAITING, BAN_PHASE, CODING, FINISHED
    private List<String> bannedLanguages = new ArrayList<>();
    private Long startTime;
    private String problemStatement = "Write a function that returns the reverse of a string. Provide input/output in standard form."; // Dummy
                                                                                                                                       // problem
                                                                                                                                       // for
                                                                                                                                       // now
    private User winner;

    public MatchRoom(String roomId) {
        this.roomId = roomId;
        this.status = "WAITING";
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public User getPlayer1() {
        return player1;
    }

    public void setPlayer1(User player1) {
        this.player1 = player1;
    }

    public User getPlayer2() {
        return player2;
    }

    public void setPlayer2(User player2) {
        this.player2 = player2;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getBannedLanguages() {
        return bannedLanguages;
    }

    public void setBannedLanguages(List<String> bannedLanguages) {
        this.bannedLanguages = bannedLanguages;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getProblemStatement() {
        return problemStatement;
    }

    public void setProblemStatement(String problemStatement) {
        this.problemStatement = problemStatement;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }
}
