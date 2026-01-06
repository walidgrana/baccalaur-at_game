package com.bac.controller;

import com.bac.network.GameClient;
import com.bac.network.GameMessage;
import com.bac.service.ConfigService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la salle d'attente multijoueur
 */
public class LobbyController implements Initializable {
    
    @FXML private Label statusLabel;
    @FXML private Label sessionCodeLabel;
    @FXML private Label letterLabel;
    @FXML private Label categoriesCountLabel;
    @FXML private Label timeLimitLabel;
    @FXML private Label playerCountLabel;
    @FXML private Label errorLabel;
    @FXML private VBox playersContainer;
    @FXML private HBox joinPane;
    @FXML private TextField codeField;
    @FXML private Button startButton;
    
    private final NavigationController navigation = NavigationController.getInstance();
    private final ConfigService configService = ConfigService.getInstance();
    
    private GameClient gameClient;
    private boolean isHost = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation de base
    }
    
    /**
     * Initialise le lobby en tant qu'hôte
     */
    public void initAsHost(GameClient client) {
        this.gameClient = client;
        this.isHost = true;
        
        statusLabel.setText("Vous êtes l'hôte de la partie");
        startButton.setVisible(true);
        startButton.setManaged(true);
        
        setupClientCallbacks();
    }
    
    /**
     * Initialise le lobby en tant qu'invité
     */
    public void initAsGuest(GameClient client, String sessionCode) {
        this.gameClient = client;
        this.isHost = false;
        
        statusLabel.setText("En attente du démarrage...");
        sessionCodeLabel.setText(sessionCode);
        
        setupClientCallbacks();
    }
    
    private void setupClientCallbacks() {
        gameClient.setOnGameCreated(this::handleGameCreated);
        gameClient.setOnPlayerJoined(this::handlePlayerJoined);
        gameClient.setOnPlayerLeft(this::handlePlayerLeft);
        gameClient.setOnPlayerList(this::handlePlayerList);
        gameClient.setOnGameStart(this::handleGameStart);
        gameClient.setOnError(this::handleError);
        gameClient.setOnDisconnect(this::handleDisconnect);
    }
    
    private void handleGameCreated(GameMessage msg) {
        Platform.runLater(() -> {
            sessionCodeLabel.setText(msg.getSessionCode());
            letterLabel.setText(String.valueOf(msg.getGameLetter()));
            
            if (msg.getCategories() != null) {
                categoriesCountLabel.setText(String.valueOf(msg.getCategories().size()));
            }
            
            int timeLimit = configService.getGameTimerSeconds();
            timeLimitLabel.setText(String.format("%d:%02d", timeLimit / 60, timeLimit % 60));
            
            updatePlayersList(List.of(gameClient.getPseudo()));
        });
    }
    
    private void handlePlayerJoined(GameMessage msg) {
        Platform.runLater(() -> {
            if (!isHost) {
                // C'est nous qui avons rejoint
                letterLabel.setText(String.valueOf(msg.getGameLetter()));
                if (msg.getCategories() != null) {
                    categoriesCountLabel.setText(String.valueOf(msg.getCategories().size()));
                }
                int timeLimit = configService.getGameTimerSeconds();
                timeLimitLabel.setText(String.format("%d:%02d", timeLimit / 60, timeLimit % 60));
            }
            hideError();
        });
    }
    
    private void handlePlayerLeft(GameMessage msg) {
        Platform.runLater(() -> {
            // La liste sera mise à jour via PLAYER_LIST
        });
    }
    
    private void handlePlayerList(GameMessage msg) {
        Platform.runLater(() -> {
            updatePlayersList(msg.getPlayers());
        });
    }
    
    private void handleGameStart(GameMessage msg) {
        Platform.runLater(() -> {
            // Naviguer vers l'écran de jeu
            GameController controller = navigation.goToGame();
            if (controller != null) {
                controller.initMultiplayerMode(
                    gameClient,
                    msg.getGameLetter(),
                    msg.getCategories(),
                    msg.getTimeRemaining()
                );
            }
        });
    }
    
    private void handleError(GameMessage msg) {
        Platform.runLater(() -> {
            showError(msg.getMessage());
        });
    }
    
    private void handleDisconnect() {
        Platform.runLater(() -> {
            showError("Déconnecté du serveur");
        });
    }
    
    private void updatePlayersList(List<String> players) {
        playersContainer.getChildren().clear();
        
        for (int i = 0; i < players.size(); i++) {
            String player = players.get(i);
            HBox row = createPlayerRow(player, i == 0 && isHost);
            playersContainer.getChildren().add(row);
        }
        
        playerCountLabel.setText(players.size() + " joueur(s)");
        
        // Activer le bouton de démarrage si au moins 2 joueurs
        if (isHost) {
            startButton.setDisable(players.size() < 2);
        }
    }
    
    private HBox createPlayerRow(String playerName, boolean isHostPlayer) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: " + (isHostPlayer ? "#e8f4fd" : "#f8f9fa") + 
                    "; -fx-background-radius: 5;");
        
        Label icon = new Label(isHostPlayer ? "👑" : "👤");
        icon.setStyle("-fx-font-size: 16px;");
        
        Label nameLabel = new Label(playerName);
        nameLabel.setStyle("-fx-font-weight: " + (isHostPlayer ? "bold" : "normal") + ";");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        if (isHostPlayer) {
            Label hostLabel = new Label("(Hôte)");
            hostLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");
            row.getChildren().addAll(icon, nameLabel, hostLabel);
        } else {
            row.getChildren().addAll(icon, nameLabel);
        }
        
        return row;
    }
    
    @FXML
    private void handleJoinWithCode() {
        String code = codeField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            showError("Veuillez entrer un code");
            return;
        }
        gameClient.joinGame(code);
    }
    
    @FXML
    private void handleStartGame() {
        if (!isHost) return;
        
        if (gameClient.getPlayers().size() < 2) {
            showError("Il faut au moins 2 joueurs pour démarrer");
            return;
        }
        
        gameClient.startGame();
    }
    
    @FXML
    private void handleQuit() {
        if (gameClient != null) {
            gameClient.leaveGame();
            gameClient.disconnect();
        }
        navigation.goToMainMenu();
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
