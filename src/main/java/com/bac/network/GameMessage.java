package com.bac.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message échangé entre le serveur et les clients
 */
public class GameMessage implements Serializable {
    
    private static final Gson gson = new GsonBuilder().create();
    
    public enum MessageType {
        // Client -> Serveur
        JOIN_GAME,          // Rejoindre une partie
        LEAVE_GAME,         // Quitter la partie
        SUBMIT_ANSWERS,     // Soumettre les réponses
        PLAYER_READY,       // Joueur prêt
        CHAT_MESSAGE,       // Message de chat
        
        // Serveur -> Client
        GAME_CREATED,       // Partie créée (contient le code)
        PLAYER_JOINED,      // Un joueur a rejoint
        PLAYER_LEFT,        // Un joueur a quitté
        GAME_START,         // Début de la partie
        GAME_END,           // Fin de la partie
        TIMER_UPDATE,       // Mise à jour du chrono
        RESULTS,            // Résultats de la partie
        ERROR,              // Erreur
        PLAYER_LIST,        // Liste des joueurs
        PLAYER_FINISHED,    // Un joueur a terminé
        
        // Bidirectionnel
        PING,
        PONG
    }
    
    private MessageType type;
    private String senderPseudo;
    private String sessionCode;
    private Character gameLetter;
    private Map<String, String> answers;
    private Map<String, Boolean> validations;
    private Map<String, Integer> scores;
    private List<String> categories;
    private List<String> players;
    private String message;
    private int timeRemaining;
    private int score;
    private boolean success;
    
    public GameMessage() {
        this.answers = new HashMap<>();
        this.validations = new HashMap<>();
        this.scores = new HashMap<>();
    }
    
    public GameMessage(MessageType type) {
        this();
        this.type = type;
    }
    
    // Méthodes de sérialisation
    public String toJson() {
        return gson.toJson(this);
    }
    
    public static GameMessage fromJson(String json) {
        return gson.fromJson(json, GameMessage.class);
    }
    
    // Constructeurs utilitaires
    public static GameMessage createJoinMessage(String pseudo, String sessionCode) {
        GameMessage msg = new GameMessage(MessageType.JOIN_GAME);
        msg.setSenderPseudo(pseudo);
        msg.setSessionCode(sessionCode);
        return msg;
    }
    
    public static GameMessage createGameCreatedMessage(String sessionCode, Character letter, List<String> categories) {
        GameMessage msg = new GameMessage(MessageType.GAME_CREATED);
        msg.setSessionCode(sessionCode);
        msg.setGameLetter(letter);
        msg.setCategories(categories);
        msg.setSuccess(true);
        return msg;
    }
    
    public static GameMessage createStartMessage(String sessionCode, Character letter, List<String> categories, int timeLimit) {
        GameMessage msg = new GameMessage(MessageType.GAME_START);
        msg.setSessionCode(sessionCode);
        msg.setGameLetter(letter);
        msg.setCategories(categories);
        msg.setTimeRemaining(timeLimit);
        return msg;
    }
    
    public static GameMessage createAnswersMessage(String pseudo, Map<String, String> answers) {
        GameMessage msg = new GameMessage(MessageType.SUBMIT_ANSWERS);
        msg.setSenderPseudo(pseudo);
        msg.setAnswers(answers);
        return msg;
    }
    
    public static GameMessage createResultsMessage(Map<String, Integer> scores, String winnerPseudo) {
        GameMessage msg = new GameMessage(MessageType.RESULTS);
        msg.setScores(scores);
        msg.setMessage(winnerPseudo);
        return msg;
    }
    
    public static GameMessage createErrorMessage(String errorMessage) {
        GameMessage msg = new GameMessage(MessageType.ERROR);
        msg.setMessage(errorMessage);
        msg.setSuccess(false);
        return msg;
    }
    
    public static GameMessage createPlayerListMessage(List<String> players) {
        GameMessage msg = new GameMessage(MessageType.PLAYER_LIST);
        msg.setPlayers(players);
        return msg;
    }
    
    public static GameMessage createTimerMessage(int secondsRemaining) {
        GameMessage msg = new GameMessage(MessageType.TIMER_UPDATE);
        msg.setTimeRemaining(secondsRemaining);
        return msg;
    }
    
    // Getters et Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSenderPseudo() {
        return senderPseudo;
    }

    public void setSenderPseudo(String senderPseudo) {
        this.senderPseudo = senderPseudo;
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

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
