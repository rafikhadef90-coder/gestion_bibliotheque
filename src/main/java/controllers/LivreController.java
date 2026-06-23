package controllers;

import dao.LivreDAO;
import models.Livre;
import utils.CSVExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class LivreController implements Initializable {

    // ─── TableView ────────────────────────────────────────────────────────────
    @FXML private TableView<Livre>            tableLivres;
    @FXML private TableColumn<Livre, Integer> colId;
    @FXML private TableColumn<Livre, String>  colTitre;
    @FXML private TableColumn<Livre, String>  colAuteur;
    @FXML private TableColumn<Livre, String>  colCategorie;
    @FXML private TableColumn<Livre, Integer> colQuantite;
    @FXML private TableColumn<Livre, Double>  colPrix;
    @FXML private TableColumn<Livre, String>  colDisponible;

    // ─── Formulaire ───────────────────────────────────────────────────────────
    @FXML private TextField   tfTitre;
    @FXML private TextField   tfAuteur;
    @FXML private TextField   tfIsbn;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private TextArea    taDescription;
    @FXML private Spinner<Integer> spQuantite;
    @FXML private Slider      sliderPrix;
    @FXML private Label       lblPrixValeur;
    @FXML private DatePicker  dpDateAjout;
    @FXML private CheckBox    ckDisponible;
    @FXML private ColorPicker colorPicker;
    @FXML private RadioButton rbDisponible;
    @FXML private RadioButton rbIndisponible;
    @FXML private ToggleGroup tgDisponibilite;

    // ─── Recherche ────────────────────────────────────────────────────────────
    @FXML private TextField    tfRecherche;
    @FXML private ComboBox<String> cbFiltreCategorie;
    @FXML private CheckBox     ckSeulementDispo;

    // ─── Stats & Progress ─────────────────────────────────────────────────────
    @FXML private Label        lblTotal;
    @FXML private Label        lblDisponibles;
    @FXML private ProgressBar  progressDispo;

    // ─── ListView ─────────────────────────────────────────────────────────────
    @FXML private ListView<String> listViewCategories;

    private final LivreDAO dao     = new LivreDAO();
    private ObservableList<Livre> livresObs = FXCollections.observableArrayList();
    private Livre livreSelectionne = null;

    // ─── Catégories disponibles ───────────────────────────────────────────────
    private static final List<String> CATEGORIES = List.of(
            "Roman", "Science", "Informatique", "Histoire",
            "Philosophie", "Jeunesse", "Biographie", "Autre"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        configurerFormulaire();
        configurerRecherche();
        chargerLivres();

        // Sélection dans la table → remplir le formulaire
        tableLivres.getSelectionModel().selectedItemProperty().addListener(
                (obs, ancien, nouveau) -> { if (nouveau != null) remplirFormulaire(nouveau); });
    }

    // ─── Configuration ────────────────────────────────────────────────────────

    private void configurerColonnes() {
        colId.setCellValueFactory       (new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory    (new PropertyValueFactory<>("titre"));
        colAuteur.setCellValueFactory   (new PropertyValueFactory<>("auteur"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colQuantite.setCellValueFactory (new PropertyValueFactory<>("quantite"));
        colPrix.setCellValueFactory     (new PropertyValueFactory<>("prix"));
        colDisponible.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().isDisponible() ? "✅ Oui" : "❌ Non"));

        // Tooltip sur les lignes
        tableLivres.setRowFactory(tv -> {
            TableRow<Livre> row = new TableRow<>();
            Tooltip tip = new Tooltip();
            row.itemProperty().addListener((obs, o, n) -> {
                if (n != null) {
                    tip.setText("ISBN : " + n.getIsbn() + "\n" + n.getDescription());
                    Tooltip.install(row, tip);
                }
            });
            return row;
        });

        tableLivres.setItems(livresObs);
    }

    private void configurerFormulaire() {
        cbCategorie.setItems(FXCollections.observableArrayList(CATEGORIES));
        cbCategorie.setValue(CATEGORIES.get(0));

        // Spinner quantité
        spQuantite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));
        spQuantite.setTooltip(new Tooltip("Nombre d'exemplaires disponibles"));

        // Slider prix
        sliderPrix.setMin(0); sliderPrix.setMax(500);
        sliderPrix.setValue(0);
        sliderPrix.valueProperty().addListener((obs, o, n) ->
                lblPrixValeur.setText(String.format("%.0f MAD", n.doubleValue())));

        dpDateAjout.setValue(LocalDate.now());

        // ToggleGroup disponibilité
        tgDisponibilite = new ToggleGroup();
        rbDisponible.setToggleGroup(tgDisponibilite);
        rbIndisponible.setToggleGroup(tgDisponibilite);
        rbDisponible.setSelected(true);

        colorPicker.setValue(Color.STEELBLUE);
        colorPicker.setTooltip(new Tooltip("Couleur d'étiquette du livre"));

        // ListView catégories
        listViewCategories.setItems(FXCollections.observableArrayList(CATEGORIES));
    }

    private void configurerRecherche() {
        List<String> filtresCat = new java.util.ArrayList<>();
        filtresCat.add("Toutes");
        filtresCat.addAll(CATEGORIES);
        cbFiltreCategorie.setItems(FXCollections.observableArrayList(filtresCat));
        cbFiltreCategorie.setValue("Toutes");

        // Recherche en temps réel
        tfRecherche.textProperty().addListener((obs, o, n) -> rechercherLivres());
        cbFiltreCategorie.valueProperty().addListener((obs, o, n) -> rechercherLivres());
        ckSeulementDispo.selectedProperty().addListener((obs, o, n) -> rechercherLivres());
    }

    // ─── Chargement données ───────────────────────────────────────────────────

    private void chargerLivres() {
        livresObs.setAll(dao.getTousLivres());
        mettreAJourStats();
    }

    private void rechercherLivres() {
        String motCle  = tfRecherche.getText();
        String cat     = cbFiltreCategorie.getValue();
        boolean dispo  = ckSeulementDispo.isSelected();
        livresObs.setAll(dao.rechercher(motCle, cat, dispo));
        mettreAJourStats();
    }

    private void mettreAJourStats() {
        int total = dao.countTotal();
        int dispo = dao.countDisponibles();
        lblTotal.setText("Total : " + total);
        lblDisponibles.setText("Disponibles : " + dispo);
        if (total > 0)
            progressDispo.setProgress((double) dispo / total);
        else
            progressDispo.setProgress(0);

        // Mettre à jour la ListView des catégories
        var repartition = dao.repartitionParCategorie();
        ObservableList<String> items = FXCollections.observableArrayList();
        repartition.forEach((k, v) -> items.add(k + " : " + v));
        listViewCategories.setItems(items);
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @FXML
    private void ajouterLivre() {
        if (!validerFormulaire()) return;
        Livre l = construireLivreDepuisFormulaire();
        if (dao.ajouter(l)) {
            afficherInfo("Succès", "Livre ajouté avec succès !");
            viderFormulaire();
            chargerLivres();
        } else {
            afficherErreur("Erreur", "Impossible d'ajouter le livre.");
        }
    }

    @FXML
    private void modifierLivre() {
        if (livreSelectionne == null) { afficherErreur("Sélection", "Veuillez sélectionner un livre."); return; }
        if (!validerFormulaire()) return;
        Livre l = construireLivreDepuisFormulaire();
        l.setId(livreSelectionne.getId());
        if (dao.modifier(l)) {
            afficherInfo("Succès", "Livre modifié avec succès !");
            viderFormulaire();
            chargerLivres();
        } else {
            afficherErreur("Erreur", "Impossible de modifier le livre.");
        }
    }

    @FXML
    private void supprimerLivre() {
        Livre selectionne = tableLivres.getSelectionModel().getSelectedItem();
        if (selectionne == null) { afficherErreur("Sélection", "Veuillez sélectionner un livre."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer « " + selectionne.getTitre() + " » ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (dao.supprimer(selectionne.getId())) {
                    afficherInfo("Succès", "Livre supprimé.");
                    viderFormulaire();
                    chargerLivres();
                } else {
                    afficherErreur("Erreur", "Impossible de supprimer (emprunt lié ?).");
                }
            }
        });
    }

    @FXML
    private void viderFormulaire() {
        livreSelectionne = null;
        tfTitre.clear(); tfAuteur.clear(); tfIsbn.clear();
        taDescription.clear();
        cbCategorie.setValue(CATEGORIES.get(0));
        spQuantite.getValueFactory().setValue(1);
        sliderPrix.setValue(0);
        dpDateAjout.setValue(LocalDate.now());
        ckDisponible.setSelected(true);
        rbDisponible.setSelected(true);
        colorPicker.setValue(Color.STEELBLUE);
    }

    // ─── Export CSV ───────────────────────────────────────────────────────────

    @FXML
    private void exporterCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter les livres");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("livres_export.csv");
        File fichier = fc.showSaveDialog(tableLivres.getScene().getWindow());
        if (fichier != null) {
            if (CSVExporter.exportLivres(livresObs, fichier.getAbsolutePath()))
                afficherInfo("Export", "Export réussi !\n" + fichier.getAbsolutePath());
            else
                afficherErreur("Export", "Erreur lors de l'export.");
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void remplirFormulaire(Livre l) {
        livreSelectionne = l;
        tfTitre.setText(l.getTitre());
        tfAuteur.setText(l.getAuteur());
        tfIsbn.setText(l.getIsbn());
        cbCategorie.setValue(l.getCategorie());
        taDescription.setText(l.getDescription());
        spQuantite.getValueFactory().setValue(l.getQuantite());
        sliderPrix.setValue(l.getPrix());
        dpDateAjout.setValue(l.getDateAjout());
        ckDisponible.setSelected(l.isDisponible());
        if (l.isDisponible()) rbDisponible.setSelected(true);
        else                  rbIndisponible.setSelected(true);
        if (l.getCouleurEtiquette() != null)
            colorPicker.setValue(Color.web(l.getCouleurEtiquette()));
    }

    private Livre construireLivreDepuisFormulaire() {
        Livre l = new Livre();
        l.setTitre      (tfTitre.getText().trim());
        l.setAuteur     (tfAuteur.getText().trim());
        l.setIsbn       (tfIsbn.getText().trim());
        l.setCategorie  (cbCategorie.getValue());
        l.setDescription(taDescription.getText().trim());
        l.setQuantite   (spQuantite.getValue());
        l.setPrix       (sliderPrix.getValue());
        l.setDateAjout  (dpDateAjout.getValue());
        l.setDisponible (rbDisponible.isSelected());
        l.setCouleurEtiquette(colorPicker.getValue().toString());
        return l;
    }

    private boolean validerFormulaire() {
        if (tfTitre.getText().isBlank()) { afficherErreur("Validation", "Le titre est obligatoire."); return false; }
        if (tfAuteur.getText().isBlank()) { afficherErreur("Validation", "L'auteur est obligatoire."); return false; }
        return true;
    }

    private void afficherInfo(String titre, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK){{ setTitle(titre); }}.show();
    }
    private void afficherErreur(String titre, String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK){{ setTitle(titre); }}.show();
    }
}