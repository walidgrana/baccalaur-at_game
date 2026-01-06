package com.bac;

import com.bac.controller.NavigationController;
import com.bac.model.dao.HibernateUtil;
import com.bac.network.GameServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Application principale Baccalauréat+
 * Jeu du Baccalauréat - لعبة الحروف
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser Hibernate (crée la base de données si nécessaire)
            System.out.println("Initialisation de la base de données...");
            HibernateUtil.getSessionFactory();
            System.out.println("Base de données initialisée avec succès !");
            
            // Configurer le stage principal
            primaryStage.setTitle("Baccalauréat+ - لعبة الحروف");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);
            
            // Configurer le contrôleur de navigation
            NavigationController navigation = NavigationController.getInstance();
            navigation.setPrimaryStage(primaryStage);
            
            // Afficher l'écran de connexion
            navigation.goToLogin();
            
            // Gérer la fermeture de l'application
            primaryStage.setOnCloseRequest(event -> {
                shutdown();
            });
            
            System.out.println("Application démarrée avec succès !");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }
    
    @Override
    public void stop() {
        shutdown();
    }
    
    /**
     * Arrête proprement tous les services
     */
    private void shutdown() {
        System.out.println("Arrêt de l'application...");
        
        // Arrêter le serveur s'il est en cours d'exécution
        try {
            GameServer server = GameServer.getInstance();
            if (server.isRunning()) {
                server.stop();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'arrêt du serveur: " + e.getMessage());
        }
        
        // Fermer la connexion Hibernate
        try {
            HibernateUtil.shutdown();
            System.out.println("Connexion à la base de données fermée.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de Hibernate: " + e.getMessage());
        }
        
        System.out.println("Application arrêtée.");
        Platform.exit();
    }
    
    /**
     * Point d'entrée de l'application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
