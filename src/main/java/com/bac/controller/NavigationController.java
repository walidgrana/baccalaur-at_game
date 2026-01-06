package com.bac.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Gestionnaire de navigation entre les vues
 */
public class NavigationController {
    
    private static NavigationController instance;
    private Stage primaryStage;
    private Scene currentScene;
    
    private NavigationController() {}
    
    public static synchronized NavigationController getInstance() {
        if (instance == null) {
            instance = new NavigationController();
        }
        return instance;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Navigue vers une vue spécifique
     */
    public void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            
            if (currentScene == null) {
                currentScene = new Scene(root);
                currentScene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
                primaryStage.setScene(currentScene);
            } else {
                currentScene.setRoot(root);
            }
            
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navigue vers une vue avec un contrôleur personnalisé
     */
    public <T> T navigateToWithController(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            
            if (currentScene == null) {
                currentScene = new Scene(root);
                currentScene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
                primaryStage.setScene(currentScene);
            } else {
                currentScene.setRoot(root);
            }
            
            primaryStage.show();
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Navigation vers l'écran de connexion
     */
    public void goToLogin() {
        navigateTo("Login.fxml");
    }
    
    /**
     * Navigation vers le menu principal
     */
    public void goToMainMenu() {
        navigateTo("MainMenu.fxml");
    }
    
    /**
     * Navigation vers le jeu
     */
    public GameController goToGame() {
        return navigateToWithController("Game.fxml");
    }
    
    /**
     * Navigation vers les résultats
     */
    public ResultsController goToResults() {
        return navigateToWithController("Results.fxml");
    }
    
    /**
     * Navigation vers le lobby multijoueur
     */
    public LobbyController goToLobby() {
        return navigateToWithController("Lobby.fxml");
    }
    
    /**
     * Navigation vers la gestion des catégories
     */
    public void goToCategories() {
        navigateTo("Categories.fxml");
    }
    
    /**
     * Navigation vers l'historique
     */
    public void goToHistory() {
        navigateTo("History.fxml");
    }
}
