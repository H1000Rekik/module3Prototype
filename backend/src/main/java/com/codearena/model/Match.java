package com.codearena.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User player1;

    @ManyToOne
    private User player2;

    @ManyToOne
    private User winner;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // LoL Mod: language that was banned
    private String bannedLanguage;

    // Chess Mod: time limit remaining for players
    private Integer player1TimeRemaining;
    private Integer player2TimeRemaining;

    // Status: WAITING, IN_PROGRESS, FINISHED, CANCELLED
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getBannedLanguage() {
        return bannedLanguage;
    }

    public void setBannedLanguage(String bannedLanguage) {
        this.bannedLanguage = bannedLanguage;
    }

    public Integer getPlayer1TimeRemaining() {
        return player1TimeRemaining;
    }

    public void setPlayer1TimeRemaining(Integer player1TimeRemaining) {
        this.player1TimeRemaining = player1TimeRemaining;
    }

    public Integer getPlayer2TimeRemaining() {
        return player2TimeRemaining;
    }

    public void setPlayer2TimeRemaining(Integer player2TimeRemaining) {
        this.player2TimeRemaining = player2TimeRemaining;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
