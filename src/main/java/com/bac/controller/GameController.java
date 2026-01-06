package com.bac.controller;

import com.bac.model.entity.Category;
import com.bac.model.entity.GameResult;
import com.bac.model.entity.GameSession;
import com.bac.network.GameClient;
import com.bac.network.GameMessage;
import com.bac.service.GameService;
import com.bac.service.ValidationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

/**
 * Contrôleur pour l'écran de jeu
 */
public class GameController implements Initializable {
    
    @FXML private Label timerLabel;
    @FXML private Label letterLabel;
    @FXML private Label modeLabel;
    @FXML private Label sessionCodeLabel;
    @FXML private VBox categoriesContainer;
    @FXML private VBox playersPane;
    @FXML private VBox playersListContainer;
    @FXML private Button submitButton;
    @FXML private Button quitButton;
    
    private final GameService gameService = GameService.getInstance();
    private final NavigationController navigation = NavigationController.getInstance();
    
    private Map<String, TextField> answerFields = new HashMap<>();
    private Map<String, Label> validationLabels = new HashMap<>();
    private Timeline timer;
    private int timeRemaining;
    private long startTime;
    
    private boolean isMultiplayer = false;
    private GameClient gameClient;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation de base
    }
    
    /**
     * Initialise le mode solo
     */
    public void initSoloMode() {
        isMultiplayer = false;
        modeLabel.setText("Mode Solo");
        sessionCodeLabel.setText("");
        
        GameSession session = gameService.getCurrentSession();
        if (session != null) {
            setupGame(session.getGameLetter(), 
                      session.getCategories().stream().map(Category::getName).toList(),
                      session.getTimeLimitSeconds());
        }
    }
    
    /**
     * Initialise le mode multijoueur
     */
    public void initMultiplayerMode(GameClient client, Character letter, List<String> categories, int timeLimit) {
        isMultiplayer = true;
        this.gameClient = client;
        modeLabel.setText("Mode Multijoueur");
        sessionCodeLabel.setText("Code: " + client.getCurrentSessionCode());
        
        // Afficher le panneau des joueurs
        playersPane.setVisible(true);
        playersPane.setManaged(true);
        
        // Configurer les callbacks du client
        setupClientCallbacks();
        
        setupGame(letter, categories, timeLimit);
    }
    
    private void setupClientCallbacks() {
        if (gameClient == null) return;
        
        gameClient.setOnTimerUpdate(msg -> {
            updateTimer(msg.getTimeRemaining());
        });
        
        gameClient.setOnPlayerFinished(msg -> {
            addPlayerFinishedNotification(msg.getSenderPseudo());
        });
        
        gameClient.setOnPlayerList(msg -> {
            updatePlayersList(msg.getPlayers());
        });
        
        gameClient.setOnResults(msg -> {
            showMultiplayerResults(msg);
        });
        
        gameClient.setOnError(msg -> {
            showError(msg.getMessage());
        });
    }
    
    private void setupGame(Character letter, List<String> categories, int timeLimitSeconds) {
        // Afficher la lettre
        letterLabel.setText(String.valueOf(letter));
        
        // Configurer le timer
        timeRemaining = timeLimitSeconds;
        startTime = System.currentTimeMillis();
        updateTimerDisplay();
        startTimer();
        
        // Créer les champs pour chaque catégorie
        categoriesContainer.getChildren().clear();
        answerFields.clear();
        validationLabels.clear();
        
        for (String categoryName : categories) {
            HBox row = createCategoryRow(categoryName, letter);
            categoriesContainer.getChildren().add(row);
        }
    }
    
    private HBox createCategoryRow(String categoryName, Character letter) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        
        // Label de la catégorie
        Label categoryLabel = new Label(categoryName);
        categoryLabel.setPrefWidth(120);
        categoryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Champ de saisie
        TextField field = new TextField();
        field.setPromptText("Mot commençant par " + letter + "...");
        field.setPrefWidth(250);
        HBox.setHgrow(field, Priority.ALWAYS);
        
        // Indicateur de validation (masqué initialement, visible seulement après validation)
        Label validationLabel = new Label("");
        validationLabel.setPrefWidth(30);
        validationLabel.setStyle("-fx-font-size: 18px;");
        validationLabel.setVisible(false);
        
        row.getChildren().addAll(categoryLabel, field, validationLabel);
        
        answerFields.put(categoryName, field);
        validationLabels.put(categoryName, validationLabel);
        
        return row;
    }
    
    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateTimerDisplay();
            
            if (timeRemaining <= 0) {
                timer.stop();
                handleTimeUp();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
    
    private void updateTimer(int seconds) {
        timeRemaining = seconds;
        updateTimerDisplay();
    }
    
    private void updateTimerDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        
        // Changer la couleur selon le temps restant
        if (timeRemaining <= 10) {
            timerLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if (timeRemaining <= 30) {
            timerLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            timerLabel.setStyle("-fx-text-fill: white;");
        }
    }
    
    private void handleTimeUp() {
        submitButton.setDisable(true);
        showInfo("Temps écoulé !");
        submitAnswers();
    }
    
    @FXML
    private void handleSubmit() {
        if (timer != null) {
            timer.stop();
        }
        submitAnswers();
    }
    
    private void submitAnswers() {
        // Désactiver le bouton et les champs pendant la validation
        submitButton.setDisable(true);
        for (TextField field : answerFields.values()) {
            field.setDisable(true);
        }
        
        // Afficher un message de chargement
        submitButton.setText("Validation en cours...");
        
        // Collecter les réponses
        Map<String, String> answers = new HashMap<>();
        for (Map.Entry<String, TextField> entry : answerFields.entrySet()) {
            answers.put(entry.getKey(), entry.getValue().getText().trim());
        }
        
        int completionTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
        Character letter = gameService.getCurrentSession() != null ? 
                          gameService.getCurrentSession().getGameLetter() : null;
        
        // Valider et afficher les résultats dans un thread séparé
        new Thread(() -> {
            // Valider chaque mot et mettre à jour l'affichage
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                String category = entry.getKey();
                String word = entry.getValue();
                TextField field = answerFields.get(category);
                Label validationLabel = validationLabels.get(category);
                
                if (word.isEmpty()) {
                    Platform.runLater(() -> {
                        validationLabel.setText("➖");
                        validationLabel.setVisible(true);
                        field.setStyle("-fx-border-color: #bdc3c7;");
                    });
                    continue;
                }
                
                // Vérifier la première lettre
                if (letter != null && !word.toUpperCase().startsWith(String.valueOf(letter))) {
                    Platform.runLater(() -> {
                        validationLabel.setText("❌");
                        validationLabel.setVisible(true);
                        field.setStyle("-fx-border-color: #e74c3c; -fx-background-color: #ffebee;");
                    });
                    continue;
                }
                
                // Valider via API
                ValidationService.ValidationResult result = 
                    ValidationService.getInstance().validateWord(word, category, letter);
                
                Platform.runLater(() -> {
                    if (result.isValid()) {
                        validationLabel.setText("✅");
                        field.setStyle("-fx-border-color: #27ae60; -fx-background-color: #e8f5e9;");
                    } else {
                        validationLabel.setText("❌");
                        field.setStyle("-fx-border-color: #e74c3c; -fx-background-color: #ffebee;");
                    }
                    validationLabel.setVisible(true);
                });
            }
            
            // Attendre un peu que l'utilisateur voie les résultats de validation
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            Platform.runLater(() -> {
                if (isMultiplayer && gameClient != null) {
                    // Envoyer les réponses au serveur
                    gameClient.submitAnswers(answers);
                } else {
                    // Mode solo - traitement local
                    GameResult result = gameService.submitAnswers(answers, completionTime);
                    gameService.endGame();
                    
                    // Afficher les résultats
                    ResultsController controller = navigation.goToResults();
                    if (controller != null) {
                        controller.showSoloResults(result, gameService.getCurrentSession().getGameLetter());
                    }
                }
            });
        }).start();
    }
    
    private void showMultiplayerResults(GameMessage msg) {
        ResultsController controller = navigation.goToResults();
        if (controller != null) {
            controller.showMultiplayerResults(msg.getScores(), msg.getMessage(), 
                gameClient.getGameLetter());
        }
        
        // Déconnecter le client
        if (gameClient != null) {
            gameClient.disconnect();
        }
    }
    
    private void updatePlayersList(List<String> players) {
        playersListContainer.getChildren().clear();
        for (String player : players) {
            Label playerLabel = new Label("👤 " + player);
            playerLabel.setStyle("-fx-font-size: 14px;");
            playersListContainer.getChildren().add(playerLabel);
        }
    }
    
    private void addPlayerFinishedNotification(String playerName) {
        // Mettre à jour l'UI pour montrer qu'un joueur a terminé
        for (javafx.scene.Node node : playersListContainer.getChildren()) {
            if (node instanceof Label label) {
                if (label.getText().contains(playerName)) {
                    label.setText("✅ " + playerName);
                    label.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60;");
                }
            }
        }
    }
    
    @FXML
    private void handleQuit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quitter");
        alert.setHeaderText("Voulez-vous vraiment quitter la partie ?");
        alert.setContentText("Votre progression sera perdue.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (timer != null) {
                timer.stop();
            }
            if (gameClient != null) {
                gameClient.leaveGame();
                gameClient.disconnect();
            }
            gameService.endGame();
            navigation.goToMainMenu();
        }
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
