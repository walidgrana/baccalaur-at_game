package com.bac.network;

import com.bac.model.entity.Category;
import com.bac.service.ConfigService;
import com.bac.service.GameService;
import com.bac.service.ValidationService;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Serveur de jeu multijoueur
 * Gère les connexions des clients et la logique de partie
 */
public class GameServer {
    
    private final int port;
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private volatile boolean running;
    
    // Sessions de jeu actives
    private final Map<String, GameRoom> gameRooms;
    
    // Instance singleton
    private static GameServer instance;
    
    public GameServer() {
        this.port = ConfigService.getInstance().getServerPort();
        this.threadPool = Executors.newCachedThreadPool();
        this.gameRooms = new ConcurrentHashMap<>();
        this.running = false;
    }
    
    public static synchronized GameServer getInstance() {
        if (instance == null) {
            instance = new GameServer();
        }
        return instance;
    }
    
    public void start() {
        if (running) {
            System.out.println("Le serveur est déjà en cours d'exécution");
            return;
        }
        
        threadPool.submit(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                System.out.println("Serveur démarré sur le port " + port);
                
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Nouveau client connecté: " + clientSocket.getInetAddress());
                        ClientHandler handler = new ClientHandler(clientSocket);
                        threadPool.submit(handler);
                    } catch (SocketException e) {
                        if (running) {
                            System.err.println("Erreur de socket: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur de démarrage du serveur: " + e.getMessage());
            }
        });
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt du serveur: " + e.getMessage());
        }
        threadPool.shutdown();
        System.out.println("Serveur arrêté");
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getPort() {
        return port;
    }
    
    /**
     * Crée une nouvelle salle de jeu
     */
    public GameRoom createRoom(String hostPseudo) {
        String sessionCode = generateSessionCode();
        GameRoom room = new GameRoom(sessionCode, hostPseudo);
        gameRooms.put(sessionCode, room);
        System.out.println("Salle créée: " + sessionCode + " par " + hostPseudo);
        return room;
    }
    
    /**
     * Obtient une salle par son code
     */
    public GameRoom getRoom(String sessionCode) {
        return gameRooms.get(sessionCode);
    }
    
    /**
     * Supprime une salle
     */
    public void removeRoom(String sessionCode) {
        gameRooms.remove(sessionCode);
        System.out.println("Salle supprimée: " + sessionCode);
    }
    
    private String generateSessionCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();
        // S'assurer que le code est unique
        while (gameRooms.containsKey(code)) {
            sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = sb.toString();
        }
        return code;
    }
    
    /**
     * Salle de jeu - gère une partie multijoueur
     */
    public static class GameRoom {
        private final String sessionCode;
        private final String hostPseudo;
        private final Map<String, ClientHandler> players;
        private final Map<String, Map<String, String>> playerAnswers;
        private final Map<String, Integer> playerScores;
        private final Set<String> playersFinished;
        private List<String> categories;
        private Character gameLetter;
        private boolean gameStarted;
        private boolean gameEnded;
        private int timeLimit;
        private ScheduledExecutorService timer;
        
        public GameRoom(String sessionCode, String hostPseudo) {
            this.sessionCode = sessionCode;
            this.hostPseudo = hostPseudo;
            this.players = new ConcurrentHashMap<>();
            this.playerAnswers = new ConcurrentHashMap<>();
            this.playerScores = new ConcurrentHashMap<>();
            this.playersFinished = ConcurrentHashMap.newKeySet();
            this.gameStarted = false;
            this.gameEnded = false;
            this.timeLimit = ConfigService.getInstance().getGameTimerSeconds();
            
            // Initialiser les catégories et la lettre
            List<Category> cats = GameService.getInstance().getActiveCategories();
            this.categories = cats.stream().map(Category::getName).collect(Collectors.toList());
            this.gameLetter = GameService.getInstance().generateRandomLetter();
        }
        
        public void addPlayer(String pseudo, ClientHandler handler) {
            players.put(pseudo, handler);
            playerScores.put(pseudo, 0);
            broadcastPlayerList();
        }
        
        public void removePlayer(String pseudo) {
            players.remove(pseudo);
            playerAnswers.remove(pseudo);
            playerScores.remove(pseudo);
            playersFinished.remove(pseudo);
            
            if (players.isEmpty()) {
                GameServer.getInstance().removeRoom(sessionCode);
            } else {
                broadcastPlayerList();
            }
        }
        
        public void startGame() {
            if (gameStarted) return;
            
            gameStarted = true;
            
            // Envoyer le message de démarrage à tous les joueurs
            GameMessage startMsg = GameMessage.createStartMessage(
                sessionCode, gameLetter, categories, timeLimit);
            broadcast(startMsg);
            
            // Démarrer le timer
            startTimer();
        }
        
        private void startTimer() {
            timer = Executors.newSingleThreadScheduledExecutor();
            final int[] remaining = {timeLimit};
            
            timer.scheduleAtFixedRate(() -> {
                remaining[0]--;
                
                // Envoyer mise à jour du timer toutes les 10 secondes
                if (remaining[0] % 10 == 0 || remaining[0] <= 10) {
                    broadcast(GameMessage.createTimerMessage(remaining[0]));
                }
                
                if (remaining[0] <= 0) {
                    endGame();
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
        
        public void submitAnswers(String pseudo, Map<String, String> answers) {
            playerAnswers.put(pseudo, answers);
            playersFinished.add(pseudo);
            
            // Calculer le score
            int score = calculateScore(answers);
            playerScores.put(pseudo, score);
            
            // Notifier les autres joueurs
            GameMessage finishedMsg = new GameMessage(GameMessage.MessageType.PLAYER_FINISHED);
            finishedMsg.setSenderPseudo(pseudo);
            broadcast(finishedMsg);
            
            // Si tous les joueurs ont terminé, fin de partie
            if (playersFinished.size() >= players.size()) {
                endGame();
            }
        }
        
        private int calculateScore(Map<String, String> answers) {
            int score = 0;
            ValidationService validationService = ValidationService.getInstance();
            
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                String word = entry.getValue();
                if (word != null && !word.trim().isEmpty()) {
                    ValidationService.ValidationResult result = 
                        validationService.validateWord(word, entry.getKey(), gameLetter);
                    if (result.isValid()) {
                        score += 10;
                    }
                }
            }
            return score;
        }
        
        public void endGame() {
            if (gameEnded) return;
            gameEnded = true;
            
            if (timer != null) {
                timer.shutdown();
            }
            
            // Déterminer le gagnant
            String winner = playerScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
            
            // Envoyer les résultats
            GameMessage resultsMsg = GameMessage.createResultsMessage(playerScores, winner);
            broadcast(resultsMsg);
            
            // Nettoyer après un délai
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                GameServer.getInstance().removeRoom(sessionCode);
            }, 30, TimeUnit.SECONDS);
        }
        
        public void broadcast(GameMessage message) {
            String json = message.toJson();
            for (ClientHandler handler : players.values()) {
                handler.send(json);
            }
        }
        
        public void broadcastPlayerList() {
            List<String> playerList = new ArrayList<>(players.keySet());
            GameMessage msg = GameMessage.createPlayerListMessage(playerList);
            broadcast(msg);
        }
        
        // Getters
        public String getSessionCode() { return sessionCode; }
        public String getHostPseudo() { return hostPseudo; }
        public List<String> getCategories() { return categories; }
        public Character getGameLetter() { return gameLetter; }
        public boolean isGameStarted() { return gameStarted; }
        public int getPlayerCount() { return players.size(); }
        public Set<String> getPlayerNames() { return players.keySet(); }
    }
    
    /**
     * Gestionnaire de client individuel
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String pseudo;
        private String currentRoomCode;
        private volatile boolean connected;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.connected = true;
        }
        
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                String line;
                while (connected && (line = in.readLine()) != null) {
                    processMessage(line);
                }
            } catch (IOException e) {
                System.err.println("Erreur de communication: " + e.getMessage());
            } finally {
                disconnect();
            }
        }
        
        private void processMessage(String json) {
            try {
                GameMessage msg = GameMessage.fromJson(json);
                
                switch (msg.getType()) {
                    case JOIN_GAME -> handleJoinGame(msg);
                    case LEAVE_GAME -> handleLeaveGame();
                    case SUBMIT_ANSWERS -> handleSubmitAnswers(msg);
                    case PLAYER_READY -> handlePlayerReady(msg);
                    case PING -> send(new GameMessage(GameMessage.MessageType.PONG).toJson());
                    default -> System.out.println("Message non géré: " + msg.getType());
                }
            } catch (Exception e) {
                System.err.println("Erreur de traitement du message: " + e.getMessage());
                send(GameMessage.createErrorMessage("Erreur de traitement: " + e.getMessage()).toJson());
            }
        }
        
        private void handleJoinGame(GameMessage msg) {
            this.pseudo = msg.getSenderPseudo();
            String roomCode = msg.getSessionCode();
            
            if (roomCode == null || roomCode.isEmpty()) {
                // Créer une nouvelle salle
                GameRoom room = createRoom(pseudo);
                this.currentRoomCode = room.getSessionCode();
                room.addPlayer(pseudo, this);
                
                GameMessage response = GameMessage.createGameCreatedMessage(
                    room.getSessionCode(), room.getGameLetter(), room.getCategories());
                send(response.toJson());
            } else {
                // Rejoindre une salle existante
                GameRoom room = getRoom(roomCode);
                if (room == null) {
                    send(GameMessage.createErrorMessage("Salle non trouvée: " + roomCode).toJson());
                    return;
                }
                if (room.isGameStarted()) {
                    send(GameMessage.createErrorMessage("La partie a déjà commencé").toJson());
                    return;
                }
                
                this.currentRoomCode = roomCode;
                room.addPlayer(pseudo, this);
                
                GameMessage response = new GameMessage(GameMessage.MessageType.PLAYER_JOINED);
                response.setSessionCode(roomCode);
                response.setGameLetter(room.getGameLetter());
                response.setCategories(room.getCategories());
                response.setSenderPseudo(pseudo);
                response.setSuccess(true);
                send(response.toJson());
            }
        }
        
        private void handleLeaveGame() {
            if (currentRoomCode != null) {
                GameRoom room = getRoom(currentRoomCode);
                if (room != null) {
                    room.removePlayer(pseudo);
                    
                    GameMessage leftMsg = new GameMessage(GameMessage.MessageType.PLAYER_LEFT);
                    leftMsg.setSenderPseudo(pseudo);
                    room.broadcast(leftMsg);
                }
            }
            currentRoomCode = null;
        }
        
        private void handleSubmitAnswers(GameMessage msg) {
            if (currentRoomCode != null) {
                GameRoom room = getRoom(currentRoomCode);
                if (room != null) {
                    room.submitAnswers(pseudo, msg.getAnswers());
                }
            }
        }
        
        private void handlePlayerReady(GameMessage msg) {
            if (currentRoomCode != null) {
                GameRoom room = getRoom(currentRoomCode);
                if (room != null && room.getHostPseudo().equals(pseudo)) {
                    // Seul l'hôte peut démarrer
                    if (room.getPlayerCount() >= 2) {
                        room.startGame();
                    } else {
                        send(GameMessage.createErrorMessage("Il faut au moins 2 joueurs").toJson());
                    }
                }
            }
        }
        
        public void send(String message) {
            if (out != null && connected) {
                out.println(message);
            }
        }
        
        private void disconnect() {
            connected = false;
            handleLeaveGame();
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
            }
            System.out.println("Client déconnecté: " + pseudo);
        }
    }
}
