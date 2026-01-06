package com.bac.controller;

import com.bac.model.entity.Category;
import com.bac.service.GameService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la gestion des catégories
 */
public class CategoriesController implements Initializable {
    
    @FXML private TextField newCategoryField;
    @FXML private VBox categoriesContainer;
    @FXML private Label infoLabel;
    
    private final GameService gameService = GameService.getInstance();
    private final NavigationController navigation = NavigationController.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategories();
        
        // Permettre l'ajout avec Entrée
        newCategoryField.setOnAction(e -> handleAddCategory());
    }
    
    private void loadCategories() {
        categoriesContainer.getChildren().clear();
        
        List<Category> categories = gameService.getAllCategories();
        
        for (Category category : categories) {
            HBox row = createCategoryRow(category);
            categoriesContainer.getChildren().add(row);
        }
        
        long activeCount = categories.stream().filter(Category::isActive).count();
        infoLabel.setText(activeCount + " catégorie(s) active(s)");
    }
    
    private HBox createCategoryRow(Category category) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        
        String bgColor = category.isActive() ? "#ffffff" : "#f0f0f0";
        row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 5; " +
                    "-fx-border-color: #e0e0e0; -fx-border-radius: 5;");
        
        // CheckBox pour activer/désactiver
        CheckBox activeCheckbox = new CheckBox();
        activeCheckbox.setSelected(category.isActive());
        activeCheckbox.setOnAction(e -> {
            gameService.toggleCategoryActive(category.getId());
            loadCategories();
        });
        
        // Nom de la catégorie
        Label nameLabel = new Label(category.getName());
        nameLabel.setStyle("-fx-font-size: 14px;" + 
                          (category.isActive() ? "" : " -fx-text-fill: #999;"));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        // Bouton éditer
        Button editButton = new Button("✏️");
        editButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditCategory(category));
        
        // Bouton supprimer
        Button deleteButton = new Button("🗑️");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteCategory(category));
        
        row.getChildren().addAll(activeCheckbox, nameLabel, editButton, deleteButton);
        return row;
    }
    
    @FXML
    private void handleAddCategory() {
        String name = newCategoryField.getText().trim();
        
        if (name.isEmpty()) {
            showError("Veuillez entrer un nom de catégorie");
            return;
        }
        
        if (name.length() < 2) {
            showError("Le nom doit contenir au moins 2 caractères");
            return;
        }
        
        try {
            gameService.addCategory(name);
            newCategoryField.clear();
            loadCategories();
        } catch (Exception e) {
            showError("Erreur lors de l'ajout: " + e.getMessage());
        }
    }
    
    private void handleEditCategory(Category category) {
        TextInputDialog dialog = new TextInputDialog(category.getName());
        dialog.setTitle("Modifier la catégorie");
        dialog.setHeaderText("Entrez le nouveau nom");
        dialog.setContentText("Nom:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                category.setName(newName.trim());
                gameService.updateCategory(category);
                loadCategories();
            }
        });
    }
    
    private void handleDeleteCategory(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer la catégorie");
        alert.setHeaderText("Voulez-vous vraiment supprimer \"" + category.getName() + "\" ?");
        alert.setContentText("Cette action est irréversible.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gameService.deleteCategory(category.getId());
                loadCategories();
            } catch (Exception e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Réinitialiser les catégories");
        alert.setHeaderText("Voulez-vous restaurer les catégories par défaut ?");
        alert.setContentText("Les catégories actuelles seront conservées mais les défauts seront ajoutés.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Réinitialiser les catégories par défaut
            String[] defaults = {"Prénom", "Animal", "Pays", "Ville", "Fruit", "Métier", "Objet", "Plante"};
            for (String name : defaults) {
                gameService.addCategory(name);
            }
            loadCategories();
        }
    }
    
    @FXML
    private void handleBack() {
        navigation.goToMainMenu();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
