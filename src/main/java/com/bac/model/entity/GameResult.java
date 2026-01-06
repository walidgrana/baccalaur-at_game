package com.bac.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entité représentant le résultat d'un joueur dans une partie
 */
@Entity
@Table(name = "game_results")
public class GameResult {
    
    @Id
    private String id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession gameSession;
    
    @Column(name = "score")
    private int score;
    
    @Column(name = "valid_words_count")
    private int validWordsCount;
    
    @Column(name = "is_winner")
    private boolean winner;
    
    @Column(name = "completion_time_seconds")
    private int completionTimeSeconds;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_result_answers", 
                     joinColumns = @JoinColumn(name = "result_id"))
    @MapKeyColumn(name = "category_name")
    @Column(name = "word")
    private Map<String, String> answers = new HashMap<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_result_validations", 
                     joinColumns = @JoinColumn(name = "result_id"))
    @MapKeyColumn(name = "category_name")
    @Column(name = "is_valid")
    private Map<String, Boolean> validations = new HashMap<>();
    
    public GameResult() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.score = 0;
        this.validWordsCount = 0;
        this.winner = false;
    }
    
    public GameResult(Player player, GameSession gameSession) {
        this();
        this.player = player;
        this.gameSession = gameSession;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public GameSession getGameSession() {
        return gameSession;
    }
    
    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getValidWordsCount() {
        return validWordsCount;
    }
    
    public void setValidWordsCount(int validWordsCount) {
        this.validWordsCount = validWordsCount;
    }
    
    public boolean isWinner() {
        return winner;
    }
    
    public void setWinner(boolean winner) {
        this.winner = winner;
    }
    
    public int getCompletionTimeSeconds() {
        return completionTimeSeconds;
    }
    
    public void setCompletionTimeSeconds(int completionTimeSeconds) {
        this.completionTimeSeconds = completionTimeSeconds;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Map<String, String> getAnswers() {
        return answers;
    }
    
    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }
    
    public Map<String, Boolean> getValidations() {
        return validations;
    }
    
    public void setValidations(Map<String, Boolean> validations) {
        this.validations = validations;
    }
    
    public void addAnswer(String category, String word, boolean isValid) {
        answers.put(category, word);
        validations.put(category, isValid);
        if (isValid) {
            validWordsCount++;
            score += 10; // 10 points par mot valide
        }
    }
    
    public void calculateScore() {
        this.score = 0;
        this.validWordsCount = 0;
        for (Map.Entry<String, Boolean> entry : validations.entrySet()) {
            if (entry.getValue()) {
                this.validWordsCount++;
                this.score += 10;
            }
        }
    }
}
