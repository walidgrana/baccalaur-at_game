package sample;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Contrôleur pour l'écran de jeu (game-view.fxml)
 */
public class GameController implements Initializable {
    
    @FXML
    private StackPane rootPane;
    
    @FXML
    private ImageView backgroundImage;
    
    @FXML
    private Label currentLetterLabel;
    
    @FXML
    private Label timerLabel;
    
    @FXML
    private Label scoreLabel;
    
    @FXML
    private TextField paysField;
    
    @FXML
    private TextField villeField;
    
    @FXML
    private TextField prenomField;
    
    @FXML
    private TextField animalField;
    
    @FXML
    private TextField fruitField;
    
    @FXML
    private TextField metierField;
    
    @FXML
    private Button validateButton;
    
    @FXML
    private Button nextRoundButton;
    
    @FXML
    private Button backButton;
    
    private char currentLetter;
    private int score = 0;
    private int timeRemaining = 60;
    private Timeline timer;
    private boolean isMultiplayer = false;
    
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Random random = new Random();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Écran de jeu chargé !");
        
        // Animation de l'arrière-plan
        startBackgroundAnimation();
        
        // Démarrer une nouvelle partie
        startNewRound();
    }
    
    /**
     * Définir le mode de jeu (solo ou multijoueur)
     */
    public void setMultiplayerMode(boolean multiplayer) {
        this.isMultiplayer = multiplayer;
    }
    
    /**
     * Démarre un nouveau tour
     */
    private void startNewRound() {
        // Générer une nouvelle lettre
        currentLetter = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        currentLetterLabel.setText(String.valueOf(currentLetter));
        
        // Réinitialiser le timer
        timeRemaining = 60;
        timerLabel.setText(String.valueOf(timeRemaining));
        
        // Vider les champs
        clearFields();
        
        // Démarrer le timer
        startTimer();
        
        // Activer le bouton valider
        validateButton.setDisable(false);
        nextRoundButton.setDisable(true);
    }
    
    /**
     * Démarre le compte à rebours
     */
    private void startTimer() {
        if (timer != null) {
            timer.stop();
        }
        
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            timerLabel.setText(String.valueOf(timeRemaining));
            
            if (timeRemaining <= 0) {
                timer.stop();
                onTimeUp();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
    
    /**
     * Appelé quand le temps est écoulé
     */
    private void onTimeUp() {
        System.out.println("Temps écoulé !");
        validateAnswers();
        validateButton.setDisable(true);
        nextRoundButton.setDisable(false);
    }
    
    /**
     * Vide tous les champs de saisie
     */
    private void clearFields() {
        paysField.clear();
        villeField.clear();
        prenomField.clear();
        animalField.clear();
        fruitField.clear();
        metierField.clear();
    }
    
    /**
     * Valide les réponses du joueur
     */
    private void validateAnswers() {
        int roundScore = 0;
        
        // Vérifier chaque réponse
        roundScore += validateAnswer(paysField.getText());
        roundScore += validateAnswer(villeField.getText());
        roundScore += validateAnswer(prenomField.getText());
        roundScore += validateAnswer(animalField.getText());
        roundScore += validateAnswer(fruitField.getText());
        roundScore += validateAnswer(metierField.getText());
        
        // Mettre à jour le score
        score += roundScore;
        scoreLabel.setText(String.valueOf(score));
        
        System.out.println("Score du tour: " + roundScore + " | Score total: " + score);
    }
    
    /**
     * Valide une réponse individuelle
     */
    private int validateAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return 0;
        }
        
        String trimmed = answer.trim().toUpperCase();
        if (trimmed.charAt(0) == currentLetter) {
            return 10; // 10 points par bonne réponse
        }
        return 0;
    }
    
    /**
     * Action du bouton Valider
     */
    @FXML
    protected void onValidateClick() {
        if (timer != null) {
            timer.stop();
        }
        validateAnswers();
        validateButton.setDisable(true);
        nextRoundButton.setDisable(false);
    }
    
    /**
     * Action du bouton Tour Suivant
     */
    @FXML
    protected void onNextRoundClick() {
        startNewRound();
    }
    
    /**
     * Action du bouton Retour
     */
    @FXML
    protected void onBackClick() {
        if (timer != null) {
            timer.stop();
        }
        SceneManager.goToModeSelection();
    }
    
    /**
     * Démarre l'animation de l'arrière-plan
     */
    private void startBackgroundAnimation() {
        Timeline bgTimeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(backgroundImage.translateXProperty(), 0),
                new KeyValue(backgroundImage.translateYProperty(), 0),
                new KeyValue(backgroundImage.scaleXProperty(), 1.0),
                new KeyValue(backgroundImage.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.seconds(4),
                new KeyValue(backgroundImage.translateXProperty(), -15),
                new KeyValue(backgroundImage.translateYProperty(), -10),
                new KeyValue(backgroundImage.scaleXProperty(), 1.05),
                new KeyValue(backgroundImage.scaleYProperty(), 1.05)
            ),
            new KeyFrame(Duration.seconds(8),
                new KeyValue(backgroundImage.translateXProperty(), 10),
                new KeyValue(backgroundImage.translateYProperty(), 5),
                new KeyValue(backgroundImage.scaleXProperty(), 1.02),
                new KeyValue(backgroundImage.scaleYProperty(), 1.02)
            ),
            new KeyFrame(Duration.seconds(12),
                new KeyValue(backgroundImage.translateXProperty(), -5),
                new KeyValue(backgroundImage.translateYProperty(), 8),
                new KeyValue(backgroundImage.scaleXProperty(), 1.04),
                new KeyValue(backgroundImage.scaleYProperty(), 1.04)
            ),
            new KeyFrame(Duration.seconds(16),
                new KeyValue(backgroundImage.translateXProperty(), 0),
                new KeyValue(backgroundImage.translateYProperty(), 0),
                new KeyValue(backgroundImage.scaleXProperty(), 1.0),
                new KeyValue(backgroundImage.scaleYProperty(), 1.0)
            )
        );
        
        bgTimeline.setCycleCount(Timeline.INDEFINITE);
        bgTimeline.play();
    }
}
