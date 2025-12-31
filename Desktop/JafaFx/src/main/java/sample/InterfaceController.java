package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Contrôleur pour l'écran d'accueil (interface.fxml)
 */
public class InterfaceController implements Initializable {
    
    @FXML
    private Button playButton;
    
    @FXML
    private TextField pseudoField;
    
    @FXML
    private ImageView settingsButton;
    
    @FXML
    private ImageView backgroundImage;
    
    private String playerPseudo;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Interface d'accueil chargée !");
        
        // Animation de l'arrière-plan (mouvement subtil)
        startBackgroundAnimation();
        
        // Écouter les changements dans le champ pseudo
        pseudoField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Activer le bouton Play uniquement si le pseudo n'est pas vide
            boolean hasValidPseudo = newValue != null && !newValue.trim().isEmpty();
            playButton.setDisable(!hasValidPseudo);
            if (hasValidPseudo) {
                playerPseudo = newValue.trim();
            }
        });
    }
    
    /**
     * Démarre l'animation de l'arrière-plan
     */
    private void startBackgroundAnimation() {
        // Animation de déplacement lent et zoom subtil
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
    
    /**
     * Retourne le pseudo du joueur
     */
    public String getPlayerPseudo() {
        return playerPseudo;
    }
    
    /**
     * Action du bouton PLAY - Navigation vers la sélection de mode
     */
    @FXML
    protected void onPlayButtonClick() {
        System.out.println("Bouton PLAY cliqué - Navigation vers sélection de mode");
        SceneManager.goToModeSelection();
    }
    
    /**
     * Action du bouton Paramètres
     */
    @FXML
    protected void onSettingsClick() {
        System.out.println("Paramètres cliqués");
        // TODO: Ouvrir les paramètres
    }
}
