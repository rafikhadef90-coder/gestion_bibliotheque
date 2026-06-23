package controllers;

import dao.EmpruntDAO;
import dao.LivreDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Tableau de bord – affiche les statistiques globales.
 */
public class DashboardController implements Initializable {

    @FXML private Label              lblTotalLivres;
    @FXML private Label              lblLivresDispo;
    @FXML private Label              lblTotalEmprunts;
    @FXML private Label              lblEmpruntsEnCours;
    @FXML private Label              lblEmpruntsEnRetard;
    @FXML private ProgressIndicator  piChargement;
    @FXML private Label              lblTauxDisponibilite;

    private final LivreDAO   livreDAO   = new LivreDAO();
    private final EmpruntDAO empruntDAO = new EmpruntDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        piChargement.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        int totalLivres     = livreDAO.countTotal();
        int livresDispo     = livreDAO.countDisponibles();
        int totalEmprunts   = empruntDAO.countTotal();
        int enCours         = empruntDAO.countEnCours();
        int enRetard        = empruntDAO.countEnRetard();

        lblTotalLivres.setText    (String.valueOf(totalLivres));
        lblLivresDispo.setText    (String.valueOf(livresDispo));
        lblTotalEmprunts.setText  (String.valueOf(totalEmprunts));
        lblEmpruntsEnCours.setText(String.valueOf(enCours));
        lblEmpruntsEnRetard.setText(String.valueOf(enRetard));

        double taux = totalLivres > 0 ? (double) livresDispo / totalLivres * 100 : 0;
        lblTauxDisponibilite.setText(String.format("%.1f %%", taux));

        piChargement.setProgress(1.0);
    }

    @FXML
    private void rafraichir() { chargerStatistiques(); }
}