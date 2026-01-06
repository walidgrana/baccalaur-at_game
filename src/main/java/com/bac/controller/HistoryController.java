package com.bac.controller;

import com.bac.model.entity.GameResult;
import com.bac.model.entity.GameSession;
import com.bac.model.entity.Player;
import com.bac.service.GameService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'historique des parties
 */
public class HistoryController implements Initializable {
    
    @FXML private Label totalGamesLabel;
    @FXML private Label totalWinsLabel;
    @FXML private Label totalScoreLabel;
    @FXML private Label avgScoreLabel;
    @FXML private TableView<GameResult> historyTable;
    @FXML private TableColumn<GameResult, String> dateColumn;
    @FXML private TableColumn<GameResult, String> modeColumn;
    @FXML private TableColumn<GameResult, String> letterColumn;
    @FXML private TableColumn<GameResult, Integer> scoreColumn;
    @FXML private TableColumn<GameResult, Integer> validWordsColumn;
    @FXML private TableColumn<GameResult, String> resultColumn;
    @FXML private VBox detailsPane;
    
    private final GameService gameService = GameService.getInstance();
    private final NavigationController navigation = NavigationController.getInstance();
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupTableSelection();
        loadData();
    }
    
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(data.getValue().getCreatedAt().format(DATE_FORMAT));
            }
            return new SimpleStringProperty("-");
        });
        
        modeColumn.setCellValueFactory(data -> {
            GameSession session = data.getValue().getGameSession();
            if (session != null && session.getGameMode() != null) {
                return new SimpleStringProperty(
                    session.getGameMode() == GameSession.GameMode.SOLO ? "Solo" : "Multi"
                );
            }
            return new SimpleStringProperty("Solo");
        });
        
        letterColumn.setCellValueFactory(data -> {
            GameSession session = data.getValue().getGameSession();
            if (session != null && session.getGameLetter() != null) {
                return new SimpleStringProperty(String.valueOf(session.getGameLetter()));
            }
            return new SimpleStringProperty("-");
        });
        
        scoreColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getScore()).asObject()
        );
        
        validWordsColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getValidWordsCount()).asObject()
        );
        
        resultColumn.setCellValueFactory(data -> {
            if (data.getValue().isWinner()) {
                return new SimpleStringProperty("🏆 Victoire");
            } else {
                GameSession session = data.getValue().getGameSession();
                if (session != null && session.getGameMode() == GameSession.GameMode.SOLO) {
                    return new SimpleStringProperty("✅ Terminé");
                }
                return new SimpleStringProperty("Partie finie");
            }
        });
    }
    
    private void setupTableSelection() {
        // Afficher les détails quand une ligne est sélectionnée
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showGameDetails(newVal);
            } else {
                clearDetails();
            }
        });
    }
    
    private void showGameDetails(GameResult result) {
        detailsPane.getChildren().clear();
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);
        
        // Titre
        Label titleLabel = new Label("📋 Détails de la partie");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        detailsPane.getChildren().add(titleLabel);
        
        // Infos générales
        GameSession session = result.getGameSession();
        String letter = session != null && session.getGameLetter() != null ? 
                       String.valueOf(session.getGameLetter()) : "?";
        
        Label infoLabel = new Label(String.format("Lettre: %s | Score: %d points | Mots valides: %d/%d",
            letter, result.getScore(), result.getValidWordsCount(), result.getAnswers().size()));
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        detailsPane.getChildren().add(infoLabel);
        
        // Séparateur
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        detailsPane.getChildren().add(separator);
        
        // Liste des réponses
        Map<String, String> answers = result.getAnswers();
        Map<String, Boolean> validations = result.getValidations();
        
        if (answers != null && !answers.isEmpty()) {
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                String category = entry.getKey();
                String word = entry.getValue();
                boolean isValid = validations != null && 
                                 validations.containsKey(category) && 
                                 validations.get(category);
                
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                
                Label catLabel = new Label(category + ":");
                catLabel.setPrefWidth(100);
                catLabel.setStyle("-fx-font-weight: bold;");
                
                Label wordLabel = new Label(word.isEmpty() ? "(vide)" : word);
                wordLabel.setStyle(word.isEmpty() ? "-fx-text-fill: #bdc3c7; -fx-font-style: italic;" : "");
                HBox.setHgrow(wordLabel, Priority.ALWAYS);
                
                Label statusLabel = new Label(word.isEmpty() ? "➖" : (isValid ? "✅" : "❌"));
                statusLabel.setStyle("-fx-font-size: 14px;");
                
                row.getChildren().addAll(catLabel, wordLabel, statusLabel);
                detailsPane.getChildren().add(row);
            }
        } else {
            Label noDataLabel = new Label("Aucun détail disponible pour cette partie");
            noDataLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            detailsPane.getChildren().add(noDataLabel);
        }
    }
    
    private void clearDetails() {
        detailsPane.getChildren().clear();
        Label placeholder = new Label("Sélectionnez une partie pour voir les détails");
        placeholder.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
        detailsPane.getChildren().add(placeholder);
    }
    
    private void loadData() {
        Player player = gameService.getCurrentPlayer();
        System.out.println("=== CHARGEMENT HISTORIQUE ===");
        System.out.println("Joueur courant: " + (player != null ? player.getPseudo() + " (ID: " + player.getId() + ")" : "NULL"));
        
        if (player == null) {
            System.err.println("Aucun joueur connecté pour l'historique");
            return;
        }
        
        // Statistiques globales
        System.out.println("Parties jouées: " + player.getGamesPlayed());
        System.out.println("Victoires: " + player.getGamesWon());
        System.out.println("Score total: " + player.getTotalScore());
        
        totalGamesLabel.setText(String.valueOf(player.getGamesPlayed()));
        totalWinsLabel.setText(String.valueOf(player.getGamesWon()));
        totalScoreLabel.setText(String.valueOf(player.getTotalScore()));
        
        if (player.getGamesPlayed() > 0) {
            int avgScore = player.getTotalScore() / player.getGamesPlayed();
            avgScoreLabel.setText(String.valueOf(avgScore));
        } else {
            avgScoreLabel.setText("0");
        }
        
        // Charger l'historique
        try {
            List<GameResult> history = gameService.getPlayerHistory();
            System.out.println("Historique chargé: " + history.size() + " parties");
            
            for (GameResult gr : history) {
                System.out.println("  - Partie ID: " + gr.getId() + ", Score: " + gr.getScore() + 
                                  ", Réponses: " + gr.getAnswers().size());
            }
            
            historyTable.getItems().clear();
            historyTable.getItems().addAll(history);
        } catch (Exception e) {
            System.err.println("Erreur chargement historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Afficher le placeholder pour les détails
        clearDetails();
    }
    
    @FXML
    private void handleBack() {
        navigation.goToMainMenu();
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
    }
}
