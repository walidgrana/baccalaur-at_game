package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger les polices personnalisées
        Font.loadFont(Main.class.getResourceAsStream("fonts/PressStart2P-Regular.ttf"), 14);
        Font.loadFont(Main.class.getResourceAsStream("fonts/Orbitron-VariableFont_wght.ttf"), 14);
        
        // Initialiser le gestionnaire de scènes
        SceneManager.setStage(primaryStage);
        
        // Charger l'interface d'accueil
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("interface.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        
        primaryStage.setTitle("Baccalauréat +");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}






































































