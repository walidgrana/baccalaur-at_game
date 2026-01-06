package com.bac.network;

import com.bac.service.ConfigService;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Client de jeu multijoueur
 * Se connecte au serveur et gère la communication
 */
public class GameClient {
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final ExecutorService executor;
    private volatile boolean connected;
    
    private String pseudo;
    private String currentSessionCode;
    private Character gameLetter;
    private List<String> categories;
    private List<String> players;
    
    // Callbacks pour les événements
    private Consumer<GameMessage> onGameCreated;
    private Consumer<GameMessage> onPlayerJoined;
    private Consumer<GameMessage> onPlayerLeft;
    private Consumer<GameMessage> onGameStart;
    private Consumer<GameMessage> onGameEnd;
    private Consumer<GameMessage> onTimerUpdate;
    private Consumer<GameMessage> onResults;
    private Consumer<GameMessage> onError;
    private Consumer<GameMessage> onPlayerList;
    private Consumer<GameMessage> onPlayerFinished;
    private Runnable onDisconnect;
    
    public GameClient(String pseudo) {
        this.pseudo = pseudo;
        this.executor = Executors.newCachedThreadPool();
        this.connected = false;
        this.categories = new ArrayList<>();
        this.players = new ArrayList<>();
    }
    
    /**
     * Se connecte au serveur local
     */
    public boolean connect() {
        return connect("localhost", ConfigService.getInstance().getServerPort());
    }
    
    /**
     * Se connecte à un serveur distant
     */
    public boolean connect(String host, int port) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            // Démarrer l'écoute des messages
            executor.submit(this::listenForMessages);
            
            System.out.println("Connecté au serveur " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Écoute les messages du serveur
     */
    private void listenForMessages() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                processMessage(line);
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Erreur de lecture: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }
    
    /**
     * Traite un message reçu du serveur
     */
    private void processMessage(String json) {
        try {
            GameMessage msg = GameMessage.fromJson(json);
            
            // Mettre à jour l'état local
            updateLocalState(msg);
            
            // Appeler le callback approprié sur le thread JavaFX
            Platform.runLater(() -> {
                switch (msg.getType()) {
                    case GAME_CREATED -> {
                        if (onGameCreated != null) onGameCreated.accept(msg);
                    }
                    case PLAYER_JOINED -> {
                        if (onPlayerJoined != null) onPlayerJoined.accept(msg);
                    }
                    case PLAYER_LEFT -> {
                        if (onPlayerLeft != null) onPlayerLeft.accept(msg);
                    }
                    case GAME_START -> {
                        if (onGameStart != null) onGameStart.accept(msg);
                    }
                    case GAME_END -> {
                        if (onGameEnd != null) onGameEnd.accept(msg);
                    }
                    case TIMER_UPDATE -> {
                        if (onTimerUpdate != null) onTimerUpdate.accept(msg);
                    }
                    case RESULTS -> {
                        if (onResults != null) onResults.accept(msg);
                    }
                    case ERROR -> {
                        if (onError != null) onError.accept(msg);
                    }
                    case PLAYER_LIST -> {
                        if (onPlayerList != null) onPlayerList.accept(msg);
                    }
                    case PLAYER_FINISHED -> {
                        if (onPlayerFinished != null) onPlayerFinished.accept(msg);
                    }
                    default -> System.out.println("Message non géré: " + msg.getType());
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur de traitement du message: " + e.getMessage());
        }
    }
    
    private void updateLocalState(GameMessage msg) {
        if (msg.getSessionCode() != null) {
            this.currentSessionCode = msg.getSessionCode();
        }
        if (msg.getGameLetter() != null) {
            this.gameLetter = msg.getGameLetter();
        }
        if (msg.getCategories() != null) {
            this.categories = msg.getCategories();
        }
        if (msg.getPlayers() != null) {
            this.players = msg.getPlayers();
        }
    }
    
    /**
     * Crée une nouvelle partie
     */
    public void createGame() {
        if (!connected) return;
        
        GameMessage msg = GameMessage.createJoinMessage(pseudo, null);
        send(msg);
    }
    
    /**
     * Rejoint une partie existante
     */
    public void joinGame(String sessionCode) {
        if (!connected) return;
        
        GameMessage msg = GameMessage.createJoinMessage(pseudo, sessionCode);
        send(msg);
    }
    
    /**
     * Quitte la partie en cours
     */
    public void leaveGame() {
        if (!connected) return;
        
        GameMessage msg = new GameMessage(GameMessage.MessageType.LEAVE_GAME);
        msg.setSenderPseudo(pseudo);
        send(msg);
        currentSessionCode = null;
    }
    
    /**
     * Envoie le signal de démarrage (hôte uniquement)
     */
    public void startGame() {
        if (!connected) return;
        
        GameMessage msg = new GameMessage(GameMessage.MessageType.PLAYER_READY);
        msg.setSenderPseudo(pseudo);
        msg.setSessionCode(currentSessionCode);
        send(msg);
    }
    
    /**
     * Soumet les réponses
     */
    public void submitAnswers(Map<String, String> answers) {
        if (!connected) return;
        
        GameMessage msg = GameMessage.createAnswersMessage(pseudo, answers);
        msg.setSessionCode(currentSessionCode);
        send(msg);
    }
    
    /**
     * Envoie un message au serveur
     */
    private void send(GameMessage msg) {
        if (out != null && connected) {
            out.println(msg.toJson());
        }
    }
    
    /**
     * Se déconnecte du serveur
     */
    public void disconnect() {
        if (!connected) return;
        
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
        }
        executor.shutdown();
        
        if (onDisconnect != null) {
            Platform.runLater(onDisconnect);
        }
        
        System.out.println("Déconnecté du serveur");
    }
    
    // ==================== Setters pour les callbacks ====================
    
    public void setOnGameCreated(Consumer<GameMessage> callback) {
        this.onGameCreated = callback;
    }
    
    public void setOnPlayerJoined(Consumer<GameMessage> callback) {
        this.onPlayerJoined = callback;
    }
    
    public void setOnPlayerLeft(Consumer<GameMessage> callback) {
        this.onPlayerLeft = callback;
    }
    
    public void setOnGameStart(Consumer<GameMessage> callback) {
        this.onGameStart = callback;
    }
    
    public void setOnGameEnd(Consumer<GameMessage> callback) {
        this.onGameEnd = callback;
    }
    
    public void setOnTimerUpdate(Consumer<GameMessage> callback) {
        this.onTimerUpdate = callback;
    }
    
    public void setOnResults(Consumer<GameMessage> callback) {
        this.onResults = callback;
    }
    
    public void setOnError(Consumer<GameMessage> callback) {
        this.onError = callback;
    }
    
    public void setOnPlayerList(Consumer<GameMessage> callback) {
        this.onPlayerList = callback;
    }
    
    public void setOnPlayerFinished(Consumer<GameMessage> callback) {
        this.onPlayerFinished = callback;
    }
    
    public void setOnDisconnect(Runnable callback) {
        this.onDisconnect = callback;
    }
    
    // ==================== Getters ====================
    
    public boolean isConnected() {
        return connected;
    }
    
    public String getPseudo() {
        return pseudo;
    }
    
    public String getCurrentSessionCode() {
        return currentSessionCode;
    }
    
    public Character getGameLetter() {
        return gameLetter;
    }
    
    public List<String> getCategories() {
        return categories;
    }
    
    public List<String> getPlayers() {
        return players;
    }
}
