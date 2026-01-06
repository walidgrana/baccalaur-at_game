package com.bac.service;

import java.util.concurrent.CompletableFuture;

/**
 * Service de validation des mots
 * Utilise uniquement les APIs externes (pas de base de données locale)
 * - APIs spécifiques par catégorie (pays, ville, prénom, etc.)
 * - API Gemini pour les autres catégories
 */
public class ValidationService {
    
    private static ValidationService instance;
    private final ApiService apiService;
    
    private ValidationService() {
        this.apiService = ApiService.getInstance();
    }
    
    public static synchronized ValidationService getInstance() {
        if (instance == null) {
            instance = new ValidationService();
        }
        return instance;
    }
    
    /**
     * Résultat de validation avec détails
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String source;
        private final String message;
        
        public ValidationResult(boolean valid, String source, String message) {
            this.valid = valid;
            this.source = source;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getSource() { return source; }
        public String getMessage() { return message; }
    }
    
    /**
     * Valide un mot pour une catégorie donnée via API
     */
    public ValidationResult validateWord(String word, String categoryName, Character requiredLetter) {
        // Vérifier que le mot n'est pas vide
        if (word == null || word.trim().isEmpty()) {
            return new ValidationResult(false, "LOCAL", "Le mot est vide");
        }
        
        String cleanWord = word.trim().toLowerCase();
        
        // Vérifier que le mot commence par la bonne lettre
        if (requiredLetter != null) {
            char firstLetter = Character.toUpperCase(cleanWord.charAt(0));
            if (firstLetter != Character.toUpperCase(requiredLetter)) {
                return new ValidationResult(false, "LOCAL", 
                    "Le mot doit commencer par la lettre " + Character.toUpperCase(requiredLetter));
            }
        }
        
        // Vérifier la longueur minimale
        if (cleanWord.length() < 2) {
            return new ValidationResult(false, "LOCAL", "Le mot doit contenir au moins 2 caractères");
        }
        
        // Validation via APIs externes
        try {
            boolean isValid = apiService.validateWord(cleanWord, categoryName);
            
            if (isValid) {
                return new ValidationResult(true, "API", "Mot validé ✓");
            } else {
                return new ValidationResult(false, "API", "Mot non reconnu pour cette catégorie");
            }
        } catch (Exception e) {
            System.err.println("Erreur de validation: " + e.getMessage());
            return new ValidationResult(false, "ERROR", "Erreur lors de la validation");
        }
    }
    
    /**
     * Validation asynchrone
     */
    public CompletableFuture<ValidationResult> validateWordAsync(String word, String categoryName, Character requiredLetter) {
        return CompletableFuture.supplyAsync(() -> validateWord(word, categoryName, requiredLetter));
    }
    
    /**
     * Validation rapide sans vérification de lettre (pour tests)
     */
    public boolean quickValidate(String word, String categoryName) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        return apiService.validateWord(word.trim(), categoryName);
    }
}
