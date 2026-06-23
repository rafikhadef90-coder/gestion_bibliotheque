package dao;

import models.Livre;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour les opérations CRUD sur la table `livres`.
 */
public class LivreDAO {

    // ─── CREATE ───────────────────────────────────────────────────────────────

    public boolean ajouter(Livre livre) {
        String sql = """
                INSERT INTO livres
                  (titre, auteur, isbn, categorie, description, quantite, prix,
                   date_ajout, disponible, couleur_etiquette)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString (1,  livre.getTitre());
            ps.setString (2,  livre.getAuteur());
            ps.setString (3,  livre.getIsbn());
            ps.setString (4,  livre.getCategorie());
            ps.setString (5,  livre.getDescription());
            ps.setInt    (6,  livre.getQuantite());
            ps.setDouble (7,  livre.getPrix());
            ps.setDate   (8,  Date.valueOf(livre.getDateAjout()));
            ps.setBoolean(9,  livre.isDisponible());
            ps.setString (10, livre.getCouleurEtiquette());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajout livre : " + e.getMessage());
            return false;
        }
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────

    public List<Livre> getTousLivres() {
        List<Livre> livres = new ArrayList<>();
        String sql = "SELECT * FROM livres ORDER BY titre";
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) livres.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Erreur lecture livres : " + e.getMessage());
        }
        return livres;
    }

    // ─── READ WITH FILTER ─────────────────────────────────────────────────────

    public List<Livre> rechercher(String motCle, String categorie, boolean seulementDisponibles) {
        List<Livre> livres = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM livres WHERE (titre LIKE ? OR auteur LIKE ? OR isbn LIKE ?)");
        if (categorie != null && !categorie.isEmpty() && !categorie.equals("Toutes"))
            sql.append(" AND categorie = ?");
        if (seulementDisponibles) sql.append(" AND disponible = true");
        sql.append(" ORDER BY titre");

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql.toString())) {
            String like = "%" + motCle + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            int idx = 4;
            if (categorie != null && !categorie.isEmpty() && !categorie.equals("Toutes"))
                ps.setString(idx, categorie);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) livres.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Erreur recherche livres : " + e.getMessage());
        }
        return livres;
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public boolean modifier(Livre livre) {
        String sql = """
                UPDATE livres SET
                  titre=?, auteur=?, isbn=?, categorie=?, description=?,
                  quantite=?, prix=?, date_ajout=?, disponible=?, couleur_etiquette=?
                WHERE id=?
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString (1,  livre.getTitre());
            ps.setString (2,  livre.getAuteur());
            ps.setString (3,  livre.getIsbn());
            ps.setString (4,  livre.getCategorie());
            ps.setString (5,  livre.getDescription());
            ps.setInt    (6,  livre.getQuantite());
            ps.setDouble (7,  livre.getPrix());
            ps.setDate   (8,  Date.valueOf(livre.getDateAjout()));
            ps.setBoolean(9,  livre.isDisponible());
            ps.setString (10, livre.getCouleurEtiquette());
            ps.setInt    (11, livre.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification livre : " + e.getMessage());
            return false;
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    public boolean supprimer(int id) {
        String sql = "DELETE FROM livres WHERE id=?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression livre : " + e.getMessage());
            return false;
        }
    }

    // ─── STATS ────────────────────────────────────────────────────────────────

    public int countTotal()        { return countSQL("SELECT COUNT(*) FROM livres"); }
    public int countDisponibles()  { return countSQL("SELECT COUNT(*) FROM livres WHERE disponible=true"); }

    public java.util.Map<String, Integer> repartitionParCategorie() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT categorie, COUNT(*) AS nb FROM livres GROUP BY categorie";
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("categorie"), rs.getInt("nb"));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return map;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private int countSQL(String sql) {
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    private Livre mapRow(ResultSet rs) throws SQLException {
        return new Livre(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("auteur"),
                rs.getString("isbn"),
                rs.getString("categorie"),
                rs.getString("description"),
                rs.getInt("quantite"),
                rs.getDouble("prix"),
                rs.getDate("date_ajout").toLocalDate(),
                rs.getBoolean("disponible"),
                rs.getString("couleur_etiquette")
        );
    }
}