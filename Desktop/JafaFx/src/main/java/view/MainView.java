package view;

import controller.MainController;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Category;

public class MainView {
    private final MainController controller;
    public MainView(MainController controller) {
        this.controller = controller;
    }
    public void start(Stage stage) {
        // Mode selection
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton soloBtn = new RadioButton("Solo");
        soloBtn.setToggleGroup(modeGroup);
        soloBtn.setSelected(true);
        RadioButton multiBtn = new RadioButton("Multijoueur");
        multiBtn.setToggleGroup(modeGroup);
        HBox modeBox = new HBox(10, soloBtn, multiBtn);

        // Random letter
        Label letterLabel = new Label("Lettre : -");
        Button letterBtn = new Button("Nouvelle lettre");
        letterBtn.setOnAction(e -> {
            controller.generateRandomLetter();
            letterLabel.setText("Lettre : " + controller.getCurrentLetter());
        });
        HBox letterBox = new HBox(10, letterLabel, letterBtn);

        // Dynamic form
        VBox formBox = new VBox(5);
        ObservableList<Category> categories = controller.getCategories();
        TextField[] fields = new TextField[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            Label catLabel = new Label(categories.get(i).getName());
            fields[i] = new TextField();
            HBox row = new HBox(10, catLabel, fields[i]);
            formBox.getChildren().add(row);
        }

        // Validate button
        Button validateBtn = new Button("Valider");
        validateBtn.setOnAction(e -> {
            ObservableList<String> words = javafx.collections.FXCollections.observableArrayList();
            for (TextField f : fields) words.add(f.getText());
            controller.validateWords(words);
        });

        VBox root = new VBox(15, modeBox, letterBox, formBox, validateBtn);
        root.setPadding(new Insets(20));
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("Baccalauréat+");
        stage.show();
    }
}
