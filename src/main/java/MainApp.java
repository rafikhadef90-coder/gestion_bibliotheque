import dao.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Point d'entrée de l'application JavaFX BiblioManager.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bibliotheque/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);

        // Feuille de style globale
        scene.getStylesheets().add(
                getClass().getResource("/com/bibliotheque/css/style.css").toExternalForm());

        primaryStage.setTitle("📚 BiblioManager — Gestion de Bibliothèque");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    @Override
    public void stop() {
        Database.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}