package com.bac.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant un joueur
 */
@Entity
@Table(name = "players")
public class Player {
    
    @Id
    private String id;
    
    @Column(name = "pseudo", unique = true, nullable = false)
    private String pseudo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "games_played")
    private int gamesPlayed;
    
    @Column(name = "games_won")
    private int gamesWon;
    
    @Column(name = "total_score")
    private int totalScore;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameResult> gameResults = new ArrayList<>();
    
    public Player() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.totalScore = 0;
    }
    
    public Player(String pseudo) {
        this();
        this.pseudo = pseudo;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPseudo() {
        return pseudo;
    }
    
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
    
    public int getGamesWon() {
        return gamesWon;
    }
    
    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
    
    public List<GameResult> getGameResults() {
        return gameResults;
    }
    
    public void setGameResults(List<GameResult> gameResults) {
        this.gameResults = gameResults;
    }
    
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }
    
    public void incrementGamesWon() {
        this.gamesWon++;
    }
    
    public void addScore(int score) {
        this.totalScore += score;
    }
    
    @Override
    public String toString() {
        return pseudo;
    }
}
