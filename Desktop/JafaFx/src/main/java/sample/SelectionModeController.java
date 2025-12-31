package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Contrôleur pour l'écran de sélection de mode (selection-mode.fxml)
 */
public class SelectionModeController implements Initializable {
    
    @FXML
    private StackPane rootPane;
    
    @FXML
    private ImageView backgroundImage;
    
    @FXML
    private HBox singlePlayerBox;
    
    @FXML
    private HBox multiPlayerBox;
    
    @FXML
    private ImageView arrowSingle;
    
    @FXML
    private ImageView arrowMulti;
    
    @FXML
    private Label label1P;
    
    @FXML
    private Label labelSingle;
    
    @FXML
    private Label label2P;
    
    @FXML
    private Label labelMulti;
    
    @FXML
    private Label backButton;
    
    // 0 = Single Player, 1 = Multiplayer
    private int selectedOption = 0;
    
    /**
     * Action du bouton retour - Retourne à la page d'accueil
     */
    @FXML
    protected void onBackClick() {
        System.out.println("Retour à la page d'accueil");
        SceneManager.goToHome();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Écran de sélection de mode chargé !");
        
        // Animation de l'arrière-plan
        startBackgroundAnimation();
        
        // Mettre le focus sur le panneau pour recevoir les événements clavier
        rootPane.setFocusTraversable(true);
        rootPane.setOnKeyPressed(this::handleKeyPress);
        
        // Demander le focus après le chargement
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                rootPane.requestFocus();
            }
        });
        
        // Initialiser l'affichage
        updateSelection();
    }
    
    /**
     * Gestion des touches clavier
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
            // Basculer entre les options
            selectedOption = (selectedOption == 0) ? 1 : 0;
            updateSelection();
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
            // Valider la sélection
            onOptionClick();
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            // Retour à l'accueil
            onBackClick();
            event.consume();
        }
    }
    
    /**
     * Met à jour l'affichage selon l'option sélectionnée
     */
    private void updateSelection() {
        if (selectedOption == 0) {
            // Single Player sélectionné
            arrowSingle.setVisible(true);
            arrowMulti.setVisible(false);
            
            // Couleurs pour Single Player (sélectionné = orange/blanc)
            label1P.getStyleClass().setAll("arcade-text-selected");
            labelSingle.getStyleClass().setAll("arcade-text-selected");
            
            // Couleurs pour Multiplayer (non sélectionné = gris)
            label2P.getStyleClass().setAll("arcade-text-unselected");
            labelMulti.getStyleClass().setAll("arcade-text-unselected");
        } else {
            // Multiplayer sélectionné
            arrowSingle.setVisible(false);
            arrowMulti.setVisible(true);
            
            // Couleurs pour Single Player (non sélectionné = gris)
            label1P.getStyleClass().setAll("arcade-text-unselected");
            labelSingle.getStyleClass().setAll("arcade-text-unselected");
            
            // Couleurs pour Multiplayer (sélectionné = orange/blanc)
            label2P.getStyleClass().setAll("arcade-text-selected");
            labelMulti.getStyleClass().setAll("arcade-text-selected");
        }
    }
    
    /**
     * Survol de Single Player avec la souris
     */
    @FXML
    protected void onSinglePlayerHover() {
        if (selectedOption != 0) {
            selectedOption = 0;
            updateSelection();
        }
    }
    
    /**
     * Survol de Multiplayer avec la souris
     */
    @FXML
    protected void onMultiPlayerHover() {
        if (selectedOption != 1) {
            selectedOption = 1;
            updateSelection();
        }
    }
    
    /**
     * Clic sur une option - valide la sélection
     */
    @FXML
    protected void onOptionClick() {
        if (selectedOption == 0) {
            System.out.println("Mode Solo sélectionné - Démarrage du jeu");
            SceneManager.goToGame();
        } else {
            System.out.println("Mode Multijoueur sélectionné - Démarrage du jeu");
            SceneManager.goToMultiplayerGame();
        }
    }
    
    /**
     * Retour à l'écran d'accueil
     */
    @FXML
    protected void onBackClick() {
        System.out.println("Retour à l'accueil");
        SceneManager.goToHome();
    }
    
    /**
     * Démarre l'animation de l'arrière-plan
     */
    private void startBackgroundAnimation() {
        Timeline timeline = new Timeline(
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
        
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
