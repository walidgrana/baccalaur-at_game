package com.bac.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une session de jeu
 */
@Entity
@Table(name = "game_sessions")
public class GameSession {
    
    @Id
    private String id;
    
    @Column(name = "session_code", unique = true)
    private String sessionCode;
    
    @Column(name = "game_letter")
    private Character gameLetter;
    
    @Column(name = "game_mode")
    @Enumerated(EnumType.STRING)
    private GameMode gameMode;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private GameStatus status;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "time_limit_seconds")
    private int timeLimitSeconds;
    
    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameResult> results = new ArrayList<>();
    
    @Transient
    private List<Category> categories = new ArrayList<>();
    
    public GameSession() {
        this.id = UUID.randomUUID().toString();
        this.startedAt = LocalDateTime.now();
        this.status = GameStatus.WAITING;
        this.timeLimitSeconds = 120;
    }
    
    public GameSession(GameMode mode) {
        this();
        this.gameMode = mode;
    }
    
    public GameSession(String sessionCode, GameMode mode) {
        this(mode);
        this.sessionCode = sessionCode;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSessionCode() {
        return sessionCode;
    }
    
    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }
    
    public Character getGameLetter() {
        return gameLetter;
    }
    
    public void setGameLetter(Character gameLetter) {
        this.gameLetter = gameLetter;
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
    
    public GameStatus getStatus() {
        return status;
    }
    
    public void setStatus(GameStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getEndedAt() {
        return endedAt;
    }
    
    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
    
    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }
    
    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }
    
    public List<GameResult> getResults() {
        return results;
    }
    
    public void setResults(List<GameResult> results) {
        this.results = results;
    }
    
    public List<Category> getCategories() {
        return categories;
    }
    
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
    
    public void start() {
        this.status = GameStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }
    
    public void end() {
        this.status = GameStatus.FINISHED;
        this.endedAt = LocalDateTime.now();
    }
    
    public enum GameMode {
        SOLO, MULTIPLAYER
    }
    
    public enum GameStatus {
        WAITING, IN_PROGRESS, FINISHED
    }
}
