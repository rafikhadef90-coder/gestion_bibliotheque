package controllers;

import dao.EmpruntDAO;
import dao.LivreDAO;
import models.Emprunt;
import models.Emprunt.Statut;
import models.Livre;
import utils.CSVExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class EmpruntController implements Initializable {

    // ─── TableView ────────────────────────────────────────────────────────────
    @FXML private TableView<Emprunt>             tableEmprunts;
    @FXML private TableColumn<Emprunt, Integer>  colId;
    @FXML private TableColumn<Emprunt, String>   colLivre;
    @FXML private TableColumn<Emprunt, String>   colEmprunteur;
    @FXML private TableColumn<Emprunt, String>   colDateEmprunt;
    @FXML private TableColumn<Emprunt, String>   colDateRetour;
    @FXML private TableColumn<Emprunt, String>   colStatut;
    @FXML private TableColumn<Emprunt, String>   colPenalite;

    // ─── Formulaire ───────────────────────────────────────────────────────────
    @FXML private ComboBox<Livre>      cbLivre;
    @FXML private TextField            tfEmprunteur;
    @FXML private TextField            tfEmail;
    @FXML private DatePicker           dpDateEmprunt;
    @FXML private DatePicker           dpDateRetourPrevue;
    @FXML private DatePicker           dpDateRetourEffective;
    @FXML private ComboBox<String>     cbStatut;
    @FXML private TextArea             taRemarques;
    @FXML private CheckBox             ckPenalite;
    @FXML private Spinner<Integer>     spRenouvellements;

    // ─── Recherche ────────────────────────────────────────────────────────────
    @FXML private TextField            tfRecherche;
    @FXML private ComboBox<String>     cbFiltreStatut;

    // ─── Stats ────────────────────────────────────────────────────────────────
    @FXML private Label       lblTotal;
    @FXML private Label       lblEnCours;
    @FXML private Label       lblEnRetard;
    @FXML private ProgressBar progressEnCours;

    private final EmpruntDAO  empruntDAO = new EmpruntDAO();
    private final LivreDAO    livreDAO   = new LivreDAO();
    private ObservableList<Emprunt> empruntsObs = FXCollections.observableArrayList();
    private Emprunt empruntSelectionne = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        configurerFormulaire();
        configurerRecherche();
        chargerEmprunts();

        tableEmprunts.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> { if (n != null) remplirFormulaire(n); });
    }

    // ─── Configuration ────────────────────────────────────────────────────────

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLivre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getLivretitre()));
        colEmprunteur.setCellValueFactory(new PropertyValueFactory<>("emprunteur"));
        colDateEmprunt.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getDateEmprunt().toString()));
        colDateRetour.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getDateRetourPrevue().toString()));
        colStatut.setCellValueFactory(cd -> {
            Statut s = cd.getValue().getStatut();
            String emoji = switch (s) {
                case EN_COURS  -> "🟡 En cours";
                case RENDU     -> "🟢 Rendu";
                case EN_RETARD -> "🔴 En retard";
            };
            return new SimpleStringProperty(emoji);
        });
        colPenalite.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().isPenaliteAppliquee() ? "⚠️ Oui" : "—"));

        tableEmprunts.setRowFactory(tv -> {
            TableRow<Emprunt> row = new TableRow<>();
            Tooltip tip = new Tooltip();
            row.itemProperty().addListener((obs, o, n) -> {
                if (n != null) {
                    tip.setText("Email : " + n.getEmail() + "\nRemarques : " + n.getRemarques());
                    Tooltip.install(row, tip);
                }
            });
            return row;
        });

        tableEmprunts.setItems(empruntsObs);
    }

    private void configurerFormulaire() {
        cbLivre.setItems(FXCollections.observableArrayList(livreDAO.getTousLivres()));
        cbLivre.setTooltip(new Tooltip("Sélectionnez le livre emprunté"));

        cbStatut.setItems(FXCollections.observableArrayList("EN_COURS", "RENDU", "EN_RETARD"));
        cbStatut.setValue("EN_COURS");

        dpDateEmprunt.setValue(LocalDate.now());
        dpDateRetourPrevue.setValue(LocalDate.now().plusDays(14));

        spRenouvellements.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        spRenouvellements.setTooltip(new Tooltip("Nombre de renouvellements"));
    }

    private void configurerRecherche() {
        cbFiltreStatut.setItems(FXCollections.observableArrayList("Tous", "EN_COURS", "RENDU", "EN_RETARD"));
        cbFiltreStatut.setValue("Tous");
        tfRecherche.textProperty().addListener((obs, o, n) -> rechercherEmprunts());
        cbFiltreStatut.valueProperty().addListener((obs, o, n) -> rechercherEmprunts());
    }

    // ─── Données ──────────────────────────────────────────────────────────────

    private void chargerEmprunts() {
        empruntsObs.setAll(empruntDAO.getTousEmprunts());
        mettreAJourStats();
    }

    private void rechercherEmprunts() {
        empruntsObs.setAll(empruntDAO.rechercher(tfRecherche.getText(), cbFiltreStatut.getValue()));
        mettreAJourStats();
    }

    private void mettreAJourStats() {
        int total    = empruntDAO.countTotal();
        int enCours  = empruntDAO.countEnCours();
        int enRetard = empruntDAO.countEnRetard();
        lblTotal.setText   ("Total : " + total);
        lblEnCours.setText ("En cours : " + enCours);
        lblEnRetard.setText("En retard : " + enRetard);
        progressEnCours.setProgress(total > 0 ? (double) enCours / total : 0);
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @FXML
    private void ajouterEmprunt() {
        if (!validerFormulaire()) return;
        Emprunt e = construireEmpruntDepuisFormulaire();
        if (empruntDAO.ajouter(e)) {
            afficherInfo("Succès", "Emprunt enregistré !");
            viderFormulaire();
            chargerEmprunts();
        } else {
            afficherErreur("Erreur", "Impossible d'enregistrer l'emprunt.");
        }
    }

    @FXML
    private void modifierEmprunt() {
        if (empruntSelectionne == null) { afficherErreur("Sélection", "Veuillez sélectionner un emprunt."); return; }
        if (!validerFormulaire()) return;
        Emprunt e = construireEmpruntDepuisFormulaire();
        e.setId(empruntSelectionne.getId());
        if (empruntDAO.modifier(e)) {
            afficherInfo("Succès", "Emprunt modifié !");
            viderFormulaire();
            chargerEmprunts();
        } else {
            afficherErreur("Erreur", "Impossible de modifier l'emprunt.");
        }
    }

    @FXML
    private void supprimerEmprunt() {
        Emprunt sel = tableEmprunts.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherErreur("Sélection", "Veuillez sélectionner un emprunt."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'emprunt de « " + sel.getEmprunteur() + " » ?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK && empruntDAO.supprimer(sel.getId())) {
                afficherInfo("Succès", "Emprunt supprimé.");
                viderFormulaire();
                chargerEmprunts();
            }
        });
    }

    @FXML
    private void marquerRendu() {
        Emprunt sel = tableEmprunts.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherErreur("Sélection", "Veuillez sélectionner un emprunt."); return; }
        sel.setStatut(Statut.RENDU);
        sel.setDateRetourEffective(LocalDate.now());
        if (empruntDAO.modifier(sel)) {
            afficherInfo("Succès", "Livre marqué comme rendu !");
            chargerEmprunts();
        }
    }

    @FXML
    private void viderFormulaire() {
        empruntSelectionne = null;
        cbLivre.setValue(null);
        tfEmprunteur.clear(); tfEmail.clear(); taRemarques.clear();
        dpDateEmprunt.setValue(LocalDate.now());
        dpDateRetourPrevue.setValue(LocalDate.now().plusDays(14));
        dpDateRetourEffective.setValue(null);
        cbStatut.setValue("EN_COURS");
        ckPenalite.setSelected(false);
        spRenouvellements.getValueFactory().setValue(0);
    }

    @FXML
    private void exporterCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter les emprunts");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("emprunts_export.csv");
        File fichier = fc.showSaveDialog(tableEmprunts.getScene().getWindow());
        if (fichier != null) {
            if (CSVExporter.exportEmprunts(empruntsObs, fichier.getAbsolutePath()))
                afficherInfo("Export", "Export réussi !\n" + fichier.getAbsolutePath());
            else
                afficherErreur("Export", "Erreur lors de l'export.");
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void remplirFormulaire(Emprunt e) {
        empruntSelectionne = e;
        cbLivre.getItems().stream()
                .filter(l -> l.getId() == e.getLivreId())
                .findFirst().ifPresent(cbLivre::setValue);
        tfEmprunteur.setText(e.getEmprunteur());
        tfEmail.setText(e.getEmail());
        dpDateEmprunt.setValue(e.getDateEmprunt());
        dpDateRetourPrevue.setValue(e.getDateRetourPrevue());
        dpDateRetourEffective.setValue(e.getDateRetourEffective());
        cbStatut.setValue(e.getStatut().name());
        taRemarques.setText(e.getRemarques());
        ckPenalite.setSelected(e.isPenaliteAppliquee());
        spRenouvellements.getValueFactory().setValue(e.getNombreRenouvellements());
    }

    private Emprunt construireEmpruntDepuisFormulaire() {
        Emprunt e = new Emprunt();
        if (cbLivre.getValue() != null) e.setLivreId(cbLivre.getValue().getId());
        e.setEmprunteur(tfEmprunteur.getText().trim());
        e.setEmail(tfEmail.getText().trim());
        e.setDateEmprunt(dpDateEmprunt.getValue());
        e.setDateRetourPrevue(dpDateRetourPrevue.getValue());
        e.setDateRetourEffective(dpDateRetourEffective.getValue());
        e.setStatut(Statut.valueOf(cbStatut.getValue()));
        e.setRemarques(taRemarques.getText().trim());
        e.setPenaliteAppliquee(ckPenalite.isSelected());
        e.setNombreRenouvellements(spRenouvellements.getValue());
        return e;
    }

    private boolean validerFormulaire() {
        if (cbLivre.getValue() == null) { afficherErreur("Validation", "Veuillez sélectionner un livre."); return false; }
        if (tfEmprunteur.getText().isBlank()) { afficherErreur("Validation", "Le nom de l'emprunteur est obligatoire."); return false; }
        return true;
    }

    private void afficherInfo(String t, String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK){{ setTitle(t); }}.show(); }
    private void afficherErreur(String t, String m) { new Alert(Alert.AlertType.ERROR, m, ButtonType.OK){{ setTitle(t); }}.show(); }
}