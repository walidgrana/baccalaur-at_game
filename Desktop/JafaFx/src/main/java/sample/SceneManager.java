package sample;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Gestionnaire de scènes pour la navigation entre les interfaces
 */
public class SceneManager {
    
    private static Stage primaryStage;
    
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }
    
    public static Stage getStage() {
        return primaryStage;
    }
    
    /**
     * Change la scène actuelle vers une nouvelle interface FXML
     * @param fxmlFile Nom du fichier FXML (ex: "interface.fxml")
     */
    public static void switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 400);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de " + fxmlFile);
            e.printStackTrace();
        }
    }
    
    /**
     * Navigation vers l'écran d'accueil
     */
    public static void goToHome() {
        switchScene("interface.fxml");
    }
    
    /**
     * Navigation vers la sélection de mode
     */
    public static void goToModeSelection() {
        switchScene("selection-mode.fxml");
    }
    
    /**
     * Navigation vers le jeu principal (mode solo)
     */
    public static void goToGame() {
        switchScene("game-view.fxml");
    }
    
    /**
     * Navigation vers le jeu multijoueur
     */
    public static void goToMultiplayerGame() {
        switchScene("game-view.fxml");
        // TODO: Configurer le mode multijoueur
    }
}
