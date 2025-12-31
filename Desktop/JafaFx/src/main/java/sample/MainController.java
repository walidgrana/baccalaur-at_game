package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

/**
 * Contrôleur pour l'écran de jeu principal (main-view.fxml)
 */
public class MainController implements Initializable {
    
    @FXML
    private Button playButton;
    
    @FXML
    private Button settingsButton;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Écran de jeu chargé !");
    }
    
    /**
     * Action du bouton PLAY
     */
    @FXML
    protected void onPlayButtonClick() {
        System.out.println("Démarrage du jeu Baccalauréat+...");
        // TODO: Logique du jeu
    }
    
    /**
     * Action du bouton Paramètres
     */
    @FXML
    protected void onSettingsClick() {
        System.out.println("Ouverture des paramètres...");
        // TODO: Ouvrir les paramètres
    }
    
    /**
     * Retour à l'écran d'accueil
     */
    @FXML
    protected void onBackClick() {
        SceneManager.goToHome();
    }
}
