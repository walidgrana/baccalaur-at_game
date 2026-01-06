package com.bac.controller;

import com.bac.model.entity.GameResult;
import com.bac.service.GameService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'écran des résultats
 */
public class ResultsController implements Initializable {
    
    @FXML private Label resultTitleLabel;
    @FXML private Label letterUsedLabel;
    @FXML private Label scoreLabel;
    @FXML private Label validCountLabel;
    @FXML private Label invalidCountLabel;
    @FXML private Label timeLabel;
    @FXML private VBox answersContainer;
    @FXML private VBox rankingPane;
    @FXML private VBox rankingContainer;
    
    private final GameService gameService = GameService.getInstance();
    private final NavigationController navigation = NavigationController.getInstance();
    
    private boolean wasMultiplayer = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation de base
    }
    
    /**
     * Affiche les résultats d'une partie solo
     */
    public void showSoloResults(GameResult result, Character letter) {
        wasMultiplayer = false;
        
        resultTitleLabel.setText("🎯 Partie terminée !");
        letterUsedLabel.setText("Lettre: " + letter);
        
        scoreLabel.setText(String.valueOf(result.getScore()));
        validCountLabel.setText(String.valueOf(result.getValidWordsCount()));
        
        int totalAnswers = result.getAnswers().size();
        int invalidCount = totalAnswers - result.getValidWordsCount();
        invalidCountLabel.setText(String.valueOf(invalidCount));
        
        int minutes = result.getCompletionTimeSeconds() / 60;
        int seconds = result.getCompletionTimeSeconds() % 60;
        timeLabel.setText(String.format("%d:%02d", minutes, seconds));
        
        // Afficher les réponses détaillées
        displayAnswers(result.getAnswers(), result.getValidations());
    }
    
    /**
     * Affiche les résultats d'une partie multijoueur
     */
    public void showMultiplayerResults(Map<String, Integer> scores, String winner, Character letter) {
        wasMultiplayer = true;
        
        String currentPseudo = gameService.getCurrentPlayer().getPseudo();
        boolean isWinner = currentPseudo.equals(winner);
        
        if (isWinner) {
            resultTitleLabel.setText("🏆 Victoire !");
        } else {
            resultTitleLabel.setText("Partie terminée");
        }
        
        letterUsedLabel.setText("Lettre: " + letter);
        
        // Afficher le score du joueur actuel
        int myScore = scores.getOrDefault(currentPseudo, 0);
        scoreLabel.setText(String.valueOf(myScore));
        
        // Afficher le classement
        rankingPane.setVisible(true);
        rankingPane.setManaged(true);
        
        displayRanking(scores, winner);
    }
    
    private void displayAnswers(Map<String, String> answers, Map<String, Boolean> validations) {
        answersContainer.getChildren().clear();
        
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String category = entry.getKey();
            String word = entry.getValue();
            boolean isValid = validations.getOrDefault(category, false);
            
            HBox row = createAnswerRow(category, word, isValid);
            answersContainer.getChildren().add(row);
        }
    }
    
    private HBox createAnswerRow(String category, String word, boolean isValid) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        
        String bgColor = isValid ? "#e8f8f0" : "#fdf2f2";
        String borderColor = isValid ? "#27ae60" : "#e74c3c";
        row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 5; " +
                     "-fx-border-color: " + borderColor + "; -fx-border-radius: 5;");
        
        Label categoryLabel = new Label(category);
        categoryLabel.setPrefWidth(120);
        categoryLabel.setStyle("-fx-font-weight: bold;");
        
        Label wordLabel = new Label(word.isEmpty() ? "(vide)" : word);
        wordLabel.setStyle(word.isEmpty() ? "-fx-text-fill: #95a5a6; -fx-font-style: italic;" : "");
        HBox.setHgrow(wordLabel, Priority.ALWAYS);
        
        Label statusLabel = new Label(isValid ? "✅ +10" : "❌ 0");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + 
                            (isValid ? "#27ae60" : "#e74c3c") + ";");
        
        row.getChildren().addAll(categoryLabel, wordLabel, statusLabel);
        return row;
    }
    
    private void displayRanking(Map<String, Integer> scores, String winner) {
        rankingContainer.getChildren().clear();
        
        // Trier par score décroissant
        scores.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .forEach(entry -> {
                String pseudo = entry.getKey();
                int score = entry.getValue();
                boolean isWinner = pseudo.equals(winner);
                
                HBox row = createRankingRow(pseudo, score, isWinner);
                rankingContainer.getChildren().add(row);
            });
    }
    
    private HBox createRankingRow(String pseudo, int score, boolean isWinner) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        
        if (isWinner) {
            row.setStyle("-fx-background-color: #fef9e7; -fx-background-radius: 5; " +
                        "-fx-border-color: #f1c40f; -fx-border-radius: 5;");
        } else {
            row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");
        }
        
        Label icon = new Label(isWinner ? "🏆" : "👤");
        icon.setStyle("-fx-font-size: 18px;");
        
        Label nameLabel = new Label(pseudo);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        Label scoreLabel = new Label(score + " pts");
        scoreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        row.getChildren().addAll(icon, nameLabel, scoreLabel);
        return row;
    }
    
    @FXML
    private void handleReplay() {
        if (wasMultiplayer) {
            // Retour au menu pour le multijoueur
            navigation.goToMainMenu();
        } else {
            // Nouvelle partie solo
            gameService.createSoloGame();
            gameService.startGame();
            
            GameController controller = navigation.goToGame();
            if (controller != null) {
                controller.initSoloMode();
            }
        }
    }
    
    @FXML
    private void handleMainMenu() {
        navigation.goToMainMenu();
    }
}
