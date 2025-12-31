package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.GameModel;
import model.Category;
import java.util.Random;

public class MainController {
    private final GameModel model;
    private final ObservableList<String> userWords = FXCollections.observableArrayList();
    public MainController(GameModel model) {
        this.model = model;
    }
    public void generateRandomLetter() {
        char letter = (char) ('A' + new Random().nextInt(26));
        model.setCurrentLetter(letter);
    }
    public char getCurrentLetter() { return model.getCurrentLetter(); }
    public ObservableList<Category> getCategories() {
        return FXCollections.observableArrayList(model.getCategories());
    }
    public void validateWords(ObservableList<String> words) {
        // Mock: always accept
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText("Tous les mots sont validés !");
        alert.showAndWait();
    }
}
