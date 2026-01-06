# Baccalauréat+ (لعبة الحروف)

## Description

Application desktop en Java/JavaFX implémentant une version modernisée du jeu du Baccalauréat (Petit Bac). L'utilisateur peut jouer seul ou affronter d'autres joueurs à distance.

## Fonctionnalités

### Mode Solo
- Lettre aléatoire générée automatiquement
- Saisie de mots pour chaque catégorie
- Validation automatique des mots via API ou base locale
- Historique des parties et statistiques

### Mode Multijoueur
- Création de parties avec code de session
- Connexion via sockets TCP
- Synchronisation de la lettre et des catégories
- Chronomètre partagé
- Classement en temps réel

### Gestion des catégories
- Catégories personnalisables (ajout/suppression/modification)
- Activation/désactivation des catégories
- Catégories par défaut : Prénom, Animal, Pays, Ville, Fruit, Métier, Objet, Plante

### Validation des mots
1. Vérification en base locale (mots déjà validés)
2. Validation via API externe configurable
3. Sauvegarde automatique des mots validés

## Technologies utilisées

- **Java 17+**
- **JavaFX 21** - Interface graphique
- **Hibernate 6** - ORM pour la persistance
- **SQLite** - Base de données locale
- **Sockets TCP** - Communication multijoueur
- **Gson** - Parsing JSON
- **Architecture MVC**

## Structure du projet

```
src/main/java/com/bac/
├── Main.java                    # Point d'entrée
├── controller/                  # Contrôleurs JavaFX
│   ├── NavigationController.java
│   ├── LoginController.java
│   ├── MainMenuController.java
│   ├── GameController.java
│   ├── ResultsController.java
│   ├── LobbyController.java
│   ├── CategoriesController.java
│   ├── HistoryController.java
│   └── SettingsController.java
├── model/
│   ├── entity/                  # Entités JPA
│   │   ├── Player.java
│   │   ├── Category.java
│   │   ├── Word.java
│   │   ├── GameSession.java
│   │   └── GameResult.java
│   └── dao/                     # Accès aux données
│       ├── HibernateUtil.java
│       ├── BaseDAO.java
│       ├── PlayerDAO.java
│       ├── CategoryDAO.java
│       ├── WordDAO.java
│       ├── GameSessionDAO.java
│       └── GameResultDAO.java
├── service/                     # Logique métier
│   ├── ConfigService.java
│   ├── GameService.java
│   ├── ValidationService.java
│   └── ApiService.java
└── network/                     # Multijoueur
    ├── GameMessage.java
    ├── GameServer.java
    └── GameClient.java

src/main/resources/
├── fxml/                        # Vues JavaFX
│   ├── Login.fxml
│   ├── MainMenu.fxml
│   ├── Game.fxml
│   ├── Results.fxml
│   ├── Lobby.fxml
│   ├── Categories.fxml
│   ├── History.fxml
│   └── Settings.fxml
├── styles/
│   └── main.css                 # Styles CSS
├── hibernate.cfg.xml            # Configuration Hibernate
└── config.properties            # Configuration application
```

## Installation et exécution

### Prérequis
- JDK 17 ou supérieur
- Maven 3.6+

### Compilation
```bash
mvn clean compile
```

### Exécution
```bash
mvn javafx:run
```

### Création du JAR exécutable
```bash
mvn clean package
```

## Configuration

### Fichier config.properties
```properties
# API de validation
api.dictionary.url=https://api.dictionaryapi.dev/api/v2/entries/en/
api.dictionary.enabled=true

# Serveur multijoueur
server.port=5555
server.timeout=30000

# Jeu
game.timer.seconds=120
```

## Utilisation

### Mode Solo
1. Entrez votre pseudo
2. Cliquez sur "Jouer en Solo"
3. Une lettre est tirée au hasard
4. Remplissez un mot par catégorie commençant par cette lettre
5. Validez vos réponses avant la fin du chrono

### Mode Multijoueur
1. **Créer une partie** : Cliquez sur "Créer" et partagez le code de session
2. **Rejoindre** : Cliquez sur "Rejoindre" et entrez le code
3. L'hôte lance la partie quand tous les joueurs sont prêts
4. Le gagnant est celui avec le plus de points

## Système de points
- **10 points** par mot valide
- Un mot est valide s'il :
  - Commence par la bonne lettre
  - Est reconnu par l'API ou existe en base locale

## Base de données
La base SQLite `baccalaureat.db` est créée automatiquement au premier lancement dans le dossier de l'application.

## Auteur
Projet de fin de module - Développement Java

## Licence
Ce projet est à usage éducatif.
