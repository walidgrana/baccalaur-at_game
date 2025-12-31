package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * Contrôleur pour l'écran d'accueil (interface.fxml)
 */
public class InterfaceController implements Initializable {
    
    @FXML
    private Button playButton;
    
    @FXML
    private ImageView settingsButton;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Interface d'accueil chargée !");
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
