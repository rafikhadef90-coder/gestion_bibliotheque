# 📚 BiblioManager — Gestion de Bibliothèque JavaFX

Application JavaFX de gestion complète d'une bibliothèque, développée dans le cadre du mini-projet GI3 ENSAO.

## Prérequis
- Java 17+
- JavaFX 21
- Maven 3.8+
- MySQL 8+

## Installation

### 1. Base de données
```bash
mysql -u root -p < sql/init_db.sql
```
Modifiez `Database.java` si besoin (URL, USER, PASSWORD).

### 2. Lancer l'application
```bash
mvn javafx:run
```

## Architecture MVC
```
src/main/java/com/bibliotheque/
├── MainApp.java                    # Point d'entrée
├── models/
│   ├── Livre.java
│   └── Emprunt.java
├── dao/
│   ├── Database.java               # Connexion MySQL (JDBC)
│   ├── LivreDAO.java
│   └── EmpruntDAO.java
├── controllers/
│   ├── MainController.java
│   ├── LivreController.java
│   ├── EmpruntController.java
│   └── DashboardController.java
└── utils/
    └── CSVExporter.java

src/main/resources/com/bibliotheque/
├── fxml/
│   ├── main.fxml
│   ├── livres.fxml
│   ├── emprunts.fxml
│   └── dashboard.fxml
└── css/
    └── style.css
```

## Fonctionnalités
- CRUD complet Livres et Emprunts
- Recherche et filtrage en temps réel
- Export CSV via FileChooser
- Tableau de bord statistiques
- Interface moderne avec sidebar de navigation