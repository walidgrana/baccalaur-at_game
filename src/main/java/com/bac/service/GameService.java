package com.bac.service;

import com.bac.model.dao.*;
import com.bac.model.entity.*;
import com.bac.model.entity.GameSession.GameMode;
import com.bac.model.entity.GameSession.GameStatus;

import java.util.*;

/**
 * Service principal de gestion du jeu
 */
public class GameService {
    
    private static GameService instance;
    
    private final PlayerDAO playerDAO;
    private final CategoryDAO categoryDAO;
    private final GameSessionDAO gameSessionDAO;
    private final GameResultDAO gameResultDAO;
    private final ValidationService validationService;
    private final ConfigService configService;
    
    private Player currentPlayer;
    private GameSession currentSession;
    
    // Lettres possibles pour le jeu (A-Z sauf lettres rares)
    private static final String AVAILABLE_LETTERS = "ABCDEFGHIJKLMNOPRSTV";
    
    private GameService() {
        this.playerDAO = new PlayerDAO();
        this.categoryDAO = new CategoryDAO();
        this.gameSessionDAO = new GameSessionDAO();
        this.gameResultDAO = new GameResultDAO();
        this.validationService = ValidationService.getInstance();
        this.configService = ConfigService.getInstance();
        
        // Initialiser les catégories par défaut de manière sécurisée
        try {
            categoryDAO.initDefaultCategories();
        } catch (Exception e) {
            System.err.println("Note: Initialisation des catégories: " + e.getMessage());
        }
    }
    
    public static synchronized GameService getInstance() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }
    
    // ==================== Gestion des joueurs ====================
    
    /**
     * Connexion d'un joueur (crée le joueur s'il n'existe pas)
     */
    public Player login(String pseudo) {
        currentPlayer = playerDAO.findOrCreate(pseudo);
        return currentPlayer;
    }
    
    /**
     * Recherche un joueur par son pseudo SANS le créer
     * Utilisé pour afficher les stats avant connexion
     */
    public Optional<Player> findPlayer(String pseudo) {
        return playerDAO.findByPseudo(pseudo);
    }
    
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void logout() {
        currentPlayer = null;
    }
    
    public List<Player> getTopPlayers(int limit) {
        return playerDAO.findTopPlayers(limit);
    }
    
    // ==================== Gestion des catégories ====================
    
    public List<Category> getActiveCategories() {
        return categoryDAO.findActiveCategories();
    }
    
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }
    
    public Category addCategory(String name) {
        return categoryDAO.findOrCreate(name);
    }
    
    public void updateCategory(Category category) {
        categoryDAO.update(category);
    }
    
    public void deleteCategory(String categoryId) {
        categoryDAO.deleteById(categoryId);
    }
    
    public void toggleCategoryActive(String categoryId) {
        categoryDAO.toggleActive(categoryId);
    }
    
    // ==================== Gestion des parties ====================
    
    public Character generateRandomLetter() {
        Random random = new Random();
        int index = random.nextInt(AVAILABLE_LETTERS.length());
        return AVAILABLE_LETTERS.charAt(index);
    }
    
    public GameSession createSoloGame() {
        currentSession = new GameSession(GameMode.SOLO);
        currentSession.setGameLetter(generateRandomLetter());
        currentSession.setCategories(getActiveCategories());
        currentSession.setTimeLimitSeconds(configService.getGameTimerSeconds());
        currentSession = gameSessionDAO.save(currentSession);
        return currentSession;
    }
    
    public GameSession createMultiplayerGame() {
        String sessionCode = gameSessionDAO.generateUniqueSessionCode();
        currentSession = new GameSession(sessionCode, GameMode.MULTIPLAYER);
        currentSession.setGameLetter(generateRandomLetter());
        currentSession.setCategories(getActiveCategories());
        currentSession.setTimeLimitSeconds(configService.getGameTimerSeconds());
        currentSession = gameSessionDAO.save(currentSession);
        return currentSession;
    }
    
    public GameSession getCurrentSession() {
        return currentSession;
    }
    
    public void startGame() {
        if (currentSession != null) {
            currentSession.start();
            gameSessionDAO.update(currentSession);
        }
    }
    
    public void endGame() {
        if (currentSession != null) {
            currentSession.end();
            gameSessionDAO.update(currentSession);
            // Ne pas effacer currentSession ici pour permettre l'affichage des résultats
        }
    }
    
    /**
     * Réinitialise la session courante (à appeler après l'affichage des résultats)
     */
    public void clearSession() {
        currentSession = null;
    }
    
    // ==================== Validation et résultats ====================
    
    public Map<String, ValidationService.ValidationResult> validateAnswers(Map<String, String> answers) {
        Map<String, ValidationService.ValidationResult> results = new HashMap<>();
        Character letter = currentSession != null ? currentSession.getGameLetter() : null;
        
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String categoryName = entry.getKey();
            String word = entry.getValue();
            
            ValidationService.ValidationResult result = validationService.validateWord(word, categoryName, letter);
            results.put(categoryName, result);
        }
        
        return results;
    }
    
    public GameResult submitAnswers(Map<String, String> answers, int completionTimeSeconds) {
        System.out.println("=== SOUMISSION DES RÉPONSES ===");
        System.out.println("Joueur: " + (currentPlayer != null ? currentPlayer.getPseudo() + " (ID: " + currentPlayer.getId() + ")" : "NULL"));
        System.out.println("Session: " + (currentSession != null ? currentSession.getId() : "NULL"));
        
        if (currentPlayer == null || currentSession == null) {
            throw new IllegalStateException("Joueur ou session non initialisé");
        }
        
        GameResult result = new GameResult(currentPlayer, currentSession);
        result.setCompletionTimeSeconds(completionTimeSeconds);
        
        Character letter = currentSession.getGameLetter();
        
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String categoryName = entry.getKey();
            String word = entry.getValue();
            
            boolean isValid = false;
            if (word != null && !word.trim().isEmpty()) {
                ValidationService.ValidationResult validationResult = 
                    validationService.validateWord(word, categoryName, letter);
                isValid = validationResult.isValid();
            }
            
            result.addAnswer(categoryName, word != null ? word : "", isValid);
        }
        
        result.calculateScore();
        result = gameResultDAO.save(result);
        System.out.println("Résultat sauvegardé: ID=" + result.getId() + ", Score=" + result.getScore());
        
        // Mettre à jour les stats du joueur
        currentPlayer.incrementGamesPlayed();
        currentPlayer.addScore(result.getScore());
        playerDAO.update(currentPlayer);
        System.out.println("Stats joueur mises à jour: Parties=" + currentPlayer.getGamesPlayed());
        
        return result;
    }
    
    public List<GameResult> getPlayerHistory() {
        if (currentPlayer == null) {
            return new ArrayList<>();
        }
        return gameResultDAO.findByPlayer(currentPlayer);
    }
    
    public List<GameResult> getRecentHistory(int limit) {
        if (currentPlayer == null) {
            return new ArrayList<>();
        }
        return gameResultDAO.findRecentByPlayer(currentPlayer, limit);
    }
    
    // ==================== Multijoueur ====================
    
    public Optional<GameSession> findGameByCode(String code) {
        return gameSessionDAO.findBySessionCode(code);
    }
    
    public List<GameSession> getWaitingGames() {
        return gameSessionDAO.findWaitingMultiplayerSessions();
    }
    
    public void setCurrentSession(GameSession session) {
        this.currentSession = session;
    }
    
    public GameSession refreshSession() {
        if (currentSession != null && currentSession.getId() != null) {
            Optional<GameSession> refreshed = gameSessionDAO.findById(currentSession.getId());
            if (refreshed.isPresent()) {
                currentSession = refreshed.get();
            }
        }
        return currentSession;
    }
}
