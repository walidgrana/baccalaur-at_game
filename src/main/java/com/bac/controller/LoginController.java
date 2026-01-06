package com.bac.controller;

import com.bac.model.entity.Player;
import com.bac.service.GameService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'écran de connexion
 */
public class LoginController implements Initializable {
    
    @FXML private TextField pseudoField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label gamesPlayedLabel;
    @FXML private Label winsLabel;
    @FXML private Label totalScoreLabel;
    
    private GameService gameService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service de jeu
        try {
            gameService = GameService.getInstance();
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation du GameService: " + e.getMessage());
        }
        
        // Permettre la connexion avec Entrée
        pseudoField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        
        // Mise à jour des stats en temps réel lors de la saisie
        pseudoField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                updatePlayerStats(newVal.trim());
            } else {
                resetStats();
            }
            hideError();
        });
        
        // Focus sur le champ pseudo
        pseudoField.requestFocus();
    }
    
    @FXML
    private void handleLogin() {
        String pseudo = pseudoField.getText().trim();
        
        if (pseudo.isEmpty()) {
            showError("Veuillez entrer un pseudo");
            return;
        }
        
        if (pseudo.length() < 2) {
            showError("Le pseudo doit contenir au moins 2 caractères");
            return;
        }
        
        if (pseudo.length() > 20) {
            showError("Le pseudo ne doit pas dépasser 20 caractères");
            return;
        }
        
        // Connexion
        try {
            Player player = gameService.login(pseudo);
            System.out.println("Joueur connecté: " + player.getPseudo());
            
            // Navigation vers le menu principal
            NavigationController.getInstance().goToMainMenu();
        } catch (Exception e) {
            showError("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updatePlayerStats(String pseudo) {
        try {
            // Chercher le joueur existant SANS le créer
            java.util.Optional<Player> playerOpt = gameService.findPlayer(pseudo);
            
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                gamesPlayedLabel.setText(String.valueOf(player.getGamesPlayed()));
                winsLabel.setText(String.valueOf(player.getGamesWon()));
                totalScoreLabel.setText(String.valueOf(player.getTotalScore()));
            } else {
                // Nouveau joueur - afficher des stats à zéro
                resetStats();
            }
        } catch (Exception e) {
            resetStats();
        }
    }
    
    private void resetStats() {
        gamesPlayedLabel.setText("0");
        winsLabel.setText("0");
        totalScoreLabel.setText("0");
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
