package models;


import java.time.LocalDate;

/**
 * Modèle représentant un Emprunt lié à un Livre.
 */
public class Emprunt {

    public enum Statut { EN_COURS, RENDU, EN_RETARD }

    private int       id;
    private int       livreId;
    private String    livretitre;   // join display
    private String    emprunteur;
    private String    email;
    private LocalDate dateEmprunt;
    private LocalDate dateRetourPrevue;
    private LocalDate dateRetourEffective;
    private Statut    statut;
    private String    remarques;
    private boolean   penaliteAppliquee;
    private int       nombreRenouvellements;

    // ─── Constructeurs ────────────────────────────────────────────────────────

    public Emprunt() {
        this.dateEmprunt       = LocalDate.now();
        this.dateRetourPrevue  = LocalDate.now().plusDays(14);
        this.statut            = Statut.EN_COURS;
        this.penaliteAppliquee = false;
        this.nombreRenouvellements = 0;
    }

    public Emprunt(int id, int livreId, String livretitre, String emprunteur,
                   String email, LocalDate dateEmprunt, LocalDate dateRetourPrevue,
                   LocalDate dateRetourEffective, Statut statut, String remarques,
                   boolean penaliteAppliquee, int nombreRenouvellements) {
        this.id                    = id;
        this.livreId               = livreId;
        this.livretitre            = livretitre;
        this.emprunteur            = emprunteur;
        this.email                 = email;
        this.dateEmprunt           = dateEmprunt;
        this.dateRetourPrevue      = dateRetourPrevue;
        this.dateRetourEffective   = dateRetourEffective;
        this.statut                = statut;
        this.remarques             = remarques;
        this.penaliteAppliquee     = penaliteAppliquee;
        this.nombreRenouvellements = nombreRenouvellements;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public int      getId()                         { return id; }
    public void     setId(int id)                   { this.id = id; }

    public int      getLivreId()                    { return livreId; }
    public void     setLivreId(int l)               { this.livreId = l; }

    public String   getLivretitre()                 { return livretitre; }
    public void     setLivretitre(String t)         { this.livretitre = t; }

    public String   getEmprunteur()                 { return emprunteur; }
    public void     setEmprunteur(String e)         { this.emprunteur = e; }

    public String   getEmail()                      { return email; }
    public void     setEmail(String e)              { this.email = e; }

    public LocalDate getDateEmprunt()               { return dateEmprunt; }
    public void      setDateEmprunt(LocalDate d)    { this.dateEmprunt = d; }

    public LocalDate getDateRetourPrevue()              { return dateRetourPrevue; }
    public void      setDateRetourPrevue(LocalDate d)   { this.dateRetourPrevue = d; }

    public LocalDate getDateRetourEffective()               { return dateRetourEffective; }
    public void      setDateRetourEffective(LocalDate d)    { this.dateRetourEffective = d; }

    public Statut   getStatut()                     { return statut; }
    public void     setStatut(Statut s)             { this.statut = s; }

    public String   getRemarques()                  { return remarques; }
    public void     setRemarques(String r)          { this.remarques = r; }

    public boolean  isPenaliteAppliquee()               { return penaliteAppliquee; }
    public void     setPenaliteAppliquee(boolean p)     { this.penaliteAppliquee = p; }

    public int      getNombreRenouvellements()              { return nombreRenouvellements; }
    public void     setNombreRenouvellements(int n)         { this.nombreRenouvellements = n; }

    @Override
    public String toString() {
        return emprunteur + " → " + livretitre;
    }
}