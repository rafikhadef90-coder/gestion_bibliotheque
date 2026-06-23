package utils;



import models.Emprunt;
import models.Livre;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Utilitaire d'export CSV pour les Livres et Emprunts.
 */
public class CSVExporter {

    private CSVExporter() {}

    public static boolean exportLivres(List<Livre> livres, String cheminFichier) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(cheminFichier))) {
            // En-tête
            pw.println("ID,Titre,Auteur,ISBN,Categorie,Quantite,Prix,DateAjout,Disponible");
            for (Livre l : livres) {
                pw.printf("%d,\"%s\",\"%s\",%s,%s,%d,%.2f,%s,%s%n",
                        l.getId(), l.getTitre(), l.getAuteur(), l.getIsbn(),
                        l.getCategorie(), l.getQuantite(), l.getPrix(),
                        l.getDateAjout(), l.isDisponible() ? "Oui" : "Non");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Erreur export livres : " + e.getMessage());
            return false;
        }
    }

    public static boolean exportEmprunts(List<Emprunt> emprunts, String cheminFichier) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(cheminFichier))) {
            pw.println("ID,Livre,Emprunteur,Email,DateEmprunt,DateRetourPrevue,DateRetourEffective,Statut,Penalite");
            for (Emprunt e : emprunts) {
                pw.printf("%d,\"%s\",\"%s\",%s,%s,%s,%s,%s,%s%n",
                        e.getId(), e.getLivretitre(), e.getEmprunteur(), e.getEmail(),
                        e.getDateEmprunt(), e.getDateRetourPrevue(),
                        e.getDateRetourEffective() != null ? e.getDateRetourEffective() : "-",
                        e.getStatut(), e.isPenaliteAppliquee() ? "Oui" : "Non");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Erreur export emprunts : " + e.getMessage());
            return false;
        }
    }
}