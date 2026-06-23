package controllers;

import dao.EmpruntDAO;
import dao.LivreDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur principal – gère la navigation dans la sidebar.
 */
public class MainController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private StackPane  contentArea;
    @FXML private Label      lblStatLivres;
    @FXML private Label      lblStatEmprunts;
    @FXML private Label      lblStatRetard;

    private final LivreDAO   livreDAO   = new LivreDAO();
    private final EmpruntDAO empruntDAO = new EmpruntDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rafraichirStats();
        chargerVue("livres"); // Vue par défaut
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @FXML private void ouvrirLivres()    { chargerVue("livres"); }
    @FXML private void ouvrirEmprunts()  { chargerVue("emprunts"); }
    @FXML private void ouvrirDashboard() { chargerVue("dashboard"); }

    private void chargerVue(String nom) {
        try {
            URL fxmlUrl = getClass().getResource("/com/bibliotheque/fxml/" + nom + ".fxml");
            if (fxmlUrl == null) return;
            javafx.scene.Node node = FXMLLoader.load(fxmlUrl);
            contentArea.getChildren().setAll(node);
            rafraichirStats();
        } catch (Exception e) {
            System.err.println("Erreur chargement vue [" + nom + "] : " + e.getMessage());
        }
    }

    // ─── Stats sidebar ────────────────────────────────────────────────────────

    public void rafraichirStats() {
        lblStatLivres.setText   (String.valueOf(livreDAO.countTotal()));
        lblStatEmprunts.setText (String.valueOf(empruntDAO.countEnCours()));
        lblStatRetard.setText   (String.valueOf(empruntDAO.countEnRetard()));
    }
}