# JavaFX Hello World

Ce projet est un exemple minimal d'application JavaFX avec une fenêtre principale, un bouton et un label.

## Structure du projet
- `src/` : Contient le code source Java
- `resources/` : Contient les ressources (fichiers FXML, images, etc.)

## Compilation et exécution

1. Assurez-vous d'avoir Java 17+ et JavaFX SDK installés.
2. Compilez le projet :
   ```sh
   javac --module-path "<chemin-vers-javafx-lib>" --add-modules javafx.controls -d out src/Main.java
   ```
3. Exécutez l'application :
   ```sh
   java --module-path "<chemin-vers-javafx-lib>" --add-modules javafx.controls -cp out Main
   ```

Remplacez `<chemin-vers-javafx-lib>` par le chemin vers le dossier `lib` du JavaFX SDK sur votre machine.

## Auteur
Généré par GitHub Copilot