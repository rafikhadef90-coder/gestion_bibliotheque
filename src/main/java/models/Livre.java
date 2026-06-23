package models;
import java.time.LocalDate;

/**
 * Modèle représentant un Livre dans la bibliothèque.
 */
public class Livre {

    private int    id;
    private String titre;
    private String auteur;
    private String isbn;
    private String categorie;
    private String description;
    private int    quantite;
    private double prix;
    private LocalDate dateAjout;
    private boolean disponible;
    private String couleurEtiquette; // ColorPicker

    // ─── Constructeurs ────────────────────────────────────────────────────────

    public Livre() {
        this.dateAjout   = LocalDate.now();
        this.disponible  = true;
        this.quantite    = 1;
    }

    public Livre(int id, String titre, String auteur, String isbn,
                 String categorie, String description, int quantite,
                 double prix, LocalDate dateAjout, boolean disponible,
                 String couleurEtiquette) {
        this.id               = id;
        this.titre            = titre;
        this.auteur           = auteur;
        this.isbn             = isbn;
        this.categorie        = categorie;
        this.description      = description;
        this.quantite         = quantite;
        this.prix             = prix;
        this.dateAjout        = dateAjout;
        this.disponible       = disponible;
        this.couleurEtiquette = couleurEtiquette;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public int    getId()               { return id; }
    public void   setId(int id)         { this.id = id; }

    public String getTitre()            { return titre; }
    public void   setTitre(String t)    { this.titre = t; }

    public String getAuteur()           { return auteur; }
    public void   setAuteur(String a)   { this.auteur = a; }

    public String getIsbn()             { return isbn; }
    public void   setIsbn(String i)     { this.isbn = i; }

    public String getCategorie()        { return categorie; }
    public void   setCategorie(String c){ this.categorie = c; }

    public String getDescription()              { return description; }
    public void   setDescription(String d)      { this.description = d; }

    public int    getQuantite()                 { return quantite; }
    public void   setQuantite(int q)            { this.quantite = q; }

    public double getPrix()                     { return prix; }
    public void   setPrix(double p)             { this.prix = p; }

    public LocalDate getDateAjout()             { return dateAjout; }
    public void      setDateAjout(LocalDate d)  { this.dateAjout = d; }

    public boolean  isDisponible()              { return disponible; }
    public void     setDisponible(boolean d)    { this.disponible = d; }

    public String getCouleurEtiquette()                   { return couleurEtiquette; }
    public void   setCouleurEtiquette(String c)           { this.couleurEtiquette = c; }

    @Override
    public String toString() { return titre + " (" + auteur + ")"; }
}
