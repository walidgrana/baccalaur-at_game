package com.bac.controller;

import com.bac.model.entity.Player;
import com.bac.network.GameClient;
import com.bac.network.GameServer;
import com.bac.service.GameService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le menu principal
 */
public class MainMenuController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    
    private final GameService gameService = GameService.getInstance();
    private final NavigationController navigation = NavigationController.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Player currentPlayer = gameService.getCurrentPlayer();
        if (currentPlayer != null) {
            welcomeLabel.setText("Bienvenue, " + currentPlayer.getPseudo() + " !");
        }
    }
    
    @FXML
    private void handleSoloGame() {
        // Créer une partie solo
        gameService.createSoloGame();
        gameService.startGame();
        
        // Naviguer vers l'écran de jeu
        GameController controller = navigation.goToGame();
        if (controller != null) {
            controller.initSoloMode();
        }
    }
    
    @FXML
    private void handleCreateMultiplayer() {
        // Démarrer le serveur si nécessaire
        GameServer server = GameServer.getInstance();
        if (!server.isRunning()) {
            server.start();
        }
        
        // Créer un client et se connecter
        Player player = gameService.getCurrentPlayer();
        GameClient client = new GameClient(player.getPseudo());
        
        if (client.connect()) {
            // Créer une nouvelle partie
            client.createGame();
            
            // Naviguer vers le lobby
            LobbyController controller = navigation.goToLobby();
            if (controller != null) {
                controller.initAsHost(client);
            }
        } else {
            showError("Impossible de se connecter au serveur");
        }
    }
    
    @FXML
    private void handleJoinMultiplayer() {
        // Demander le code de la partie
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rejoindre une partie");
        dialog.setHeaderText("Entrez le code de la partie");
        dialog.setContentText("Code:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> {
            if (code.trim().isEmpty()) {
                showError("Veuillez entrer un code valide");
                return;
            }
            
            // Créer un client et se connecter
            Player player = gameService.getCurrentPlayer();
            GameClient client = new GameClient(player.getPseudo());
            
            if (client.connect()) {
                // Rejoindre la partie
                client.joinGame(code.trim().toUpperCase());
                
                // Naviguer vers le lobby
                LobbyController controller = navigation.goToLobby();
                if (controller != null) {
                    controller.initAsGuest(client, code.trim().toUpperCase());
                }
            } else {
                showError("Impossible de se connecter au serveur");
            }
        });
    }
    
    @FXML
    private void handleHistory() {
        navigation.goToHistory();
    }
    
    @FXML
    private void handleCategories() {
        navigation.goToCategories();
    }
    
    @FXML
    private void handleLogout() {
        gameService.logout();
        navigation.goToLogin();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
