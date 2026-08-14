package com.mtech.adms.dao;

import com.mtech.adms.exception.DataAccessException;
import com.mtech.adms.model.Site;
import com.mtech.adms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Full CRUD for sites. Phase 10 originally added a minimal version
 * (findAll only) to support the Project form's site multi-select -
 * this phase completes it with insert/update/search, matching the
 * EmployeeDao/ProjectDao pattern. findAll() below now returns ALL
 * sites (active and inactive) since this is the management screen;
 * the Project form's dropdown should keep using the active-only
 * behavior, so a dedicated findAllActive() is kept for that purpose.
 */
public class SiteDao implements GenericDao<Site, Integer> {

    private static final String BASE_SELECT =
            "SELECT id, site_name, address, city, is_active, created_at, updated_at FROM sites ";

    @Override
    public Site insert(Site site) {
        String sql = "INSERT INTO sites (site_name, address, city) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, site.getSiteName());
            stmt.setString(2, site.getAddress());
            stmt.setString(3, site.getCity());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating site failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    site.setId(keys.getInt(1));
                }
            }
            return site;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert site: " + site.getSiteName(), e);
        }
    }

    @Override
    public boolean update(Site site) {
        String sql = "UPDATE sites SET site_name = ?, address = ?, city = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, site.getSiteName());
            stmt.setString(2, site.getAddress());
            stmt.setString(3, site.getCity());
            stmt.setBoolean(4, site.isActive());
            stmt.setInt(5, site.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update site id: " + site.getId(), e);
        }
    }

    @Override
    public Optional<Site> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find site by id: " + id, e);
        }
    }

    /**
     * Returns ALL sites regardless of active status - used by the
     * Site Management screen, where inactive sites still need to be
     * visible (so they can be reactivated).
     */
    @Override
    public List<Site> findAll() {
        return queryList(BASE_SELECT + "ORDER BY site_name", null);
    }

    /**
     * Returns only active sites - used by the Project form's
     * multi-select, where inactive sites shouldn't be selectable
     * for new project assignments.
     */
    public List<Site> findAllActive() {
        return queryList(BASE_SELECT + "WHERE is_active = TRUE ORDER BY site_name", null);
    }

    public List<Site> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = BASE_SELECT + "WHERE site_name LIKE ? OR city LIKE ? ORDER BY site_name";
        String pattern = "%" + keyword.trim() + "%";
        return queryList(sql, new String[]{pattern, pattern});
    }

    private List<Site> queryList(String sql, String[] params) {
        List<Site> sites = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sites.add(mapRow(rs));
                }
            }
            return sites;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch sites", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        throw new UnsupportedOperationException(
                "Sites are not hard-deleted. Use update() with is_active=false instead.");
    }

    private Site mapRow(ResultSet rs) throws SQLException {
        Site site = new Site();
        site.setId(rs.getInt("id"));
        site.setSiteName(rs.getString("site_name"));
        site.setAddress(rs.getString("address"));
        site.setCity(rs.getString("city"));
        site.setActive(rs.getBoolean("is_active"));
        return site;
    }
}