package dao;



import models.Emprunt;
import models.Emprunt.Statut;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour les opérations CRUD sur la table `emprunts`.
 */
public class EmpruntDAO {

    // ─── CREATE ───────────────────────────────────────────────────────────────

    public boolean ajouter(Emprunt e) {
        String sql = """
                INSERT INTO emprunts
                  (livre_id, emprunteur, email, date_emprunt, date_retour_prevue,
                   date_retour_effective, statut, remarques,
                   penalite_appliquee, nombre_renouvellements)
                VALUES (?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt    (1,  e.getLivreId());
            ps.setString (2,  e.getEmprunteur());
            ps.setString (3,  e.getEmail());
            ps.setDate   (4,  Date.valueOf(e.getDateEmprunt()));
            ps.setDate   (5,  Date.valueOf(e.getDateRetourPrevue()));
            ps.setDate   (6,  e.getDateRetourEffective() != null ? Date.valueOf(e.getDateRetourEffective()) : null);
            ps.setString (7,  e.getStatut().name());
            ps.setString (8,  e.getRemarques());
            ps.setBoolean(9,  e.isPenaliteAppliquee());
            ps.setInt    (10, e.getNombreRenouvellements());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur ajout emprunt : " + ex.getMessage());
            return false;
        }
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────

    public List<Emprunt> getTousEmprunts() {
        List<Emprunt> list = new ArrayList<>();
        String sql = """
                SELECT e.*, l.titre AS livre_titre
                FROM emprunts e
                JOIN livres l ON e.livre_id = l.id
                ORDER BY e.date_emprunt DESC
                """;
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            System.err.println("Erreur lecture emprunts : " + ex.getMessage());
        }
        return list;
    }

    // ─── READ BY FILTER ───────────────────────────────────────────────────────

    public List<Emprunt> rechercher(String motCle, String statut) {
        List<Emprunt> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT e.*, l.titre AS livre_titre
                FROM emprunts e
                JOIN livres l ON e.livre_id = l.id
                WHERE (e.emprunteur LIKE ? OR l.titre LIKE ? OR e.email LIKE ?)
                """);
        if (statut != null && !statut.isEmpty() && !statut.equals("Tous"))
            sql.append(" AND e.statut = ?");
        sql.append(" ORDER BY e.date_emprunt DESC");

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql.toString())) {
            String like = "%" + motCle + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            if (statut != null && !statut.isEmpty() && !statut.equals("Tous"))
                ps.setString(4, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException ex) {
            System.err.println("Erreur recherche emprunts : " + ex.getMessage());
        }
        return list;
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public boolean modifier(Emprunt e) {
        String sql = """
                UPDATE emprunts SET
                  livre_id=?, emprunteur=?, email=?, date_emprunt=?,
                  date_retour_prevue=?, date_retour_effective=?, statut=?,
                  remarques=?, penalite_appliquee=?, nombre_renouvellements=?
                WHERE id=?
                """;
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt    (1,  e.getLivreId());
            ps.setString (2,  e.getEmprunteur());
            ps.setString (3,  e.getEmail());
            ps.setDate   (4,  Date.valueOf(e.getDateEmprunt()));
            ps.setDate   (5,  Date.valueOf(e.getDateRetourPrevue()));
            ps.setDate   (6,  e.getDateRetourEffective() != null ? Date.valueOf(e.getDateRetourEffective()) : null);
            ps.setString (7,  e.getStatut().name());
            ps.setString (8,  e.getRemarques());
            ps.setBoolean(9,  e.isPenaliteAppliquee());
            ps.setInt    (10, e.getNombreRenouvellements());
            ps.setInt    (11, e.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur modification emprunt : " + ex.getMessage());
            return false;
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    public boolean supprimer(int id) {
        String sql = "DELETE FROM emprunts WHERE id=?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur suppression emprunt : " + ex.getMessage());
            return false;
        }
    }

    // ─── STATS ────────────────────────────────────────────────────────────────

    public int countTotal()       { return countSQL("SELECT COUNT(*) FROM emprunts"); }
    public int countEnCours()     { return countSQL("SELECT COUNT(*) FROM emprunts WHERE statut='EN_COURS'"); }
    public int countEnRetard()    { return countSQL("SELECT COUNT(*) FROM emprunts WHERE statut='EN_RETARD'"); }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private int countSQL(String sql) {
        try (Statement st = Database.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    private Emprunt mapRow(ResultSet rs) throws SQLException {
        Date eff = rs.getDate("date_retour_effective");
        return new Emprunt(
                rs.getInt("id"),
                rs.getInt("livre_id"),
                rs.getString("livre_titre"),
                rs.getString("emprunteur"),
                rs.getString("email"),
                rs.getDate("date_emprunt").toLocalDate(),
                rs.getDate("date_retour_prevue").toLocalDate(),
                eff != null ? eff.toLocalDate() : null,
                Statut.valueOf(rs.getString("statut")),
                rs.getString("remarques"),
                rs.getBoolean("penalite_appliquee"),
                rs.getInt("nombre_renouvellements")
        );
    }
}
