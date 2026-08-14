package com.mtech.adms.dao;

import com.mtech.adms.exception.DataAccessException;
import com.mtech.adms.model.AssetCategory;
import com.mtech.adms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssetCategoryDao implements GenericDao<AssetCategory, Integer> {

    private static final String BASE_SELECT =
            "SELECT id, name, description, is_active, created_at, updated_at FROM asset_categories ";

    @Override
    public AssetCategory insert(AssetCategory category) {
        String sql = "INSERT INTO asset_categories (name, description) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating asset category failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
            return category;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert asset category: " + category.getName(), e);
        }
    }

    @Override
    public boolean update(AssetCategory category) {
        String sql = "UPDATE asset_categories SET name = ?, description = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBoolean(3, category.isActive());
            stmt.setInt(4, category.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update asset category id: " + category.getId(), e);
        }
    }

    @Override
    public Optional<AssetCategory> findById(Integer id) {
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
            throw new DataAccessException("Failed to find asset category by id: " + id, e);
        }
    }

    /**
     * Returns ALL categories regardless of active status - used by
     * the management screen, where inactive categories still need
     * to be visible so they can be reactivated.
     */
    @Override
    public List<AssetCategory> findAll() {
        return queryList(BASE_SELECT + "ORDER BY name", null);
    }

    /**
     * Returns only active categories - used by the Asset form's
     * category dropdown (Phase 13), where inactive categories
     * shouldn't be assignable to new/edited assets.
     */
    public List<AssetCategory> findAllActive() {
        return queryList(BASE_SELECT + "WHERE is_active = TRUE ORDER BY name", null);
    }

    public List<AssetCategory> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = BASE_SELECT + "WHERE name LIKE ? OR description LIKE ? ORDER BY name";
        String pattern = "%" + keyword.trim() + "%";
        return queryList(sql, new String[]{pattern, pattern});
    }

    private List<AssetCategory> queryList(String sql, String[] params) {
        List<AssetCategory> categories = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapRow(rs));
                }
            }
            return categories;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch asset categories", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        throw new UnsupportedOperationException(
                "Asset categories are not hard-deleted. Use update() with is_active=false instead.");
    }

    private AssetCategory mapRow(ResultSet rs) throws SQLException {
        AssetCategory category = new AssetCategory();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setActive(rs.getBoolean("is_active"));
        category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        category.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return category;
    }
}