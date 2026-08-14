package com.mtech.adms.dao;

import com.mtech.adms.exception.DataAccessException;
import com.mtech.adms.model.Asset;
import com.mtech.adms.model.AssetMovement;
import com.mtech.adms.util.Constants;
import com.mtech.adms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssetDao implements GenericDao<Asset, Integer> {

    private static final String BASE_SELECT =
            "SELECT a.id, a.asset_id, a.tool_name, a.serial_number, a.category_id, " +
                    "c.name AS category_name, a.condition_status, a.status, a.current_deployment_id, " +
                    "a.current_location, a.purchase_date, a.purchase_cost, a.qr_code_path, a.notes, " +
                    "a.created_at, a.updated_at " +
                    "FROM assets a JOIN asset_categories c ON a.category_id = c.id ";

    private final AssetMovementDao movementDao = new AssetMovementDao();

    @Override
    public Asset insert(Asset asset) {
        // Creating an asset also needs to log its initial movement -
        // use insertWithInitialMovement() so both happen in one transaction.
        throw new UnsupportedOperationException(
                "Use insertWithInitialMovement() so the asset and its first movement log are saved together.");
    }

    /**
     * Inserts a new asset and logs its initial movement (NULL -> AVAILABLE)
     * as a single transaction.
     */
    public Asset insertWithInitialMovement(Asset asset, int movedByUserId) {
        String sql = "INSERT INTO assets " +
                "(asset_id, tool_name, serial_number, category_id, condition_status, " +
                "status, current_location, purchase_date, purchase_cost, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, asset.getAssetId());
                    stmt.setString(2, asset.getToolName());
                    stmt.setString(3, asset.getSerialNumber());
                    stmt.setInt(4, asset.getCategoryId());
                    stmt.setString(5, asset.getConditionStatus());
                    stmt.setString(6, asset.getStatus());
                    stmt.setString(7, asset.getCurrentLocation());
                    stmt.setObject(8, asset.getPurchaseDate());
                    stmt.setBigDecimal(9, asset.getPurchaseCost());
                    stmt.setString(10, asset.getNotes());

                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            asset.setId(keys.getInt(1));
                        }
                    }
                }

                AssetMovement movement = new AssetMovement();
                movement.setAssetId(asset.getId());
                movement.setFromStatus(null);
                movement.setToStatus(asset.getStatus());
                movement.setFromLocation(null);
                movement.setToLocation(asset.getCurrentLocation());
                movement.setMovedBy(movedByUserId);
                movement.setNotes("Asset created.");
                movementDao.insert(conn, movement);

                conn.commit();
                return asset;

            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed to create asset: " + asset.getToolName(), e);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create asset: " + asset.getToolName(), e);
        }
    }

    @Override
    public boolean update(Asset asset) {
        // Descriptive-field-only update (name, serial, category, condition,
        // purchase info, notes). Does NOT change status or location -
        // use changeStatus() for that, since status changes must be logged.
        String sql = "UPDATE assets SET tool_name = ?, serial_number = ?, category_id = ?, " +
                "condition_status = ?, purchase_date = ?, purchase_cost = ?, notes = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asset.getToolName());
            stmt.setString(2, asset.getSerialNumber());
            stmt.setInt(3, asset.getCategoryId());
            stmt.setString(4, asset.getConditionStatus());
            stmt.setObject(5, asset.getPurchaseDate());
            stmt.setBigDecimal(6, asset.getPurchaseCost());
            stmt.setString(7, asset.getNotes());
            stmt.setInt(8, asset.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update asset id: " + asset.getId(), e);
        }
    }

    /**
     * Changes an asset's status and location, logging the transition to
     * asset_movements in the same transaction. This is the ONLY way
     * status should ever change outside of the deployment workflow
     * (Phase 14), which will have its own dedicated transition logic.
     */
    public void changeStatus(int assetId, String newStatus, String newLocation,
                             int movedByUserId, String notes) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String currentStatus;
                String currentLocation;

                try (PreparedStatement select = conn.prepareStatement(
                        "SELECT status, current_location FROM assets WHERE id = ? FOR UPDATE")) {
                    select.setInt(1, assetId);
                    try (ResultSet rs = select.executeQuery()) {
                        if (!rs.next()) {
                            throw new DataAccessException("Asset not found: id " + assetId);
                        }
                        currentStatus = rs.getString("status");
                        currentLocation = rs.getString("current_location");
                    }
                }

                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE assets SET status = ?, current_location = ? WHERE id = ?")) {
                    update.setString(1, newStatus);
                    update.setString(2, newLocation);
                    update.setInt(3, assetId);
                    update.executeUpdate();
                }

                AssetMovement movement = new AssetMovement();
                movement.setAssetId(assetId);
                movement.setFromStatus(currentStatus);
                movement.setToStatus(newStatus);
                movement.setFromLocation(currentLocation);
                movement.setToLocation(newLocation);
                movement.setMovedBy(movedByUserId);
                movement.setNotes(notes);
                movementDao.insert(conn, movement);

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed to change status for asset id: " + assetId, e);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to change status for asset id: " + assetId, e);
        }
    }

    @Override
    public Optional<Asset> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE a.id = ?";

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
            throw new DataAccessException("Failed to find asset by id: " + id, e);
        }
    }

    @Override
    public List<Asset> findAll() {
        return queryList(BASE_SELECT + "ORDER BY a.created_at DESC", null);
    }

    public List<Asset> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = BASE_SELECT +
                "WHERE a.asset_id LIKE ? OR a.tool_name LIKE ? OR a.serial_number LIKE ? " +
                "ORDER BY a.created_at DESC";
        String pattern = "%" + keyword.trim() + "%";
        return queryList(sql, new String[]{pattern, pattern, pattern});
    }

    /**
     * Filters by status in addition to the search keyword. Passing null
     * or "ALL" for status returns all statuses.
     */
    public List<Asset> searchByStatus(String keyword, String status) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null && !status.isBlank() && !"ALL".equals(status);

        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (hasKeyword) {
            sql.append("AND (a.asset_id LIKE ? OR a.tool_name LIKE ? OR a.serial_number LIKE ?) ");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (hasStatus) {
            sql.append("AND a.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY a.created_at DESC");

        return queryList(sql.toString(), params.toArray(new String[0]));
    }

    private List<Asset> queryList(String sql, String[] params) {
        List<Asset> assets = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assets.add(mapRow(rs));
                }
            }
            return assets;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch assets", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        throw new UnsupportedOperationException(
                "Assets are not hard-deleted. Use changeStatus() to set status to RETIRED instead.");
    }

    public String getNextAssetId() {
        String sql = "SELECT asset_id FROM assets WHERE asset_id LIKE 'AST-%' ORDER BY id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int nextNumber = 1;
            if (rs.next()) {
                String lastId = rs.getString("asset_id");
                String numericPart = lastId.substring(lastId.lastIndexOf('-') + 1);
                nextNumber = Integer.parseInt(numericPart) + 1;
            }
            return String.format("AST-%05d", nextNumber);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate next asset ID", e);
        }
    }

    private Asset mapRow(ResultSet rs) throws SQLException {
        Asset asset = new Asset();
        asset.setId(rs.getInt("id"));
        asset.setAssetId(rs.getString("asset_id"));
        asset.setToolName(rs.getString("tool_name"));
        asset.setSerialNumber(rs.getString("serial_number"));
        asset.setCategoryId(rs.getInt("category_id"));
        asset.setCategoryName(rs.getString("category_name"));
        asset.setConditionStatus(rs.getString("condition_status"));
        asset.setStatus(rs.getString("status"));

        int depId = rs.getInt("current_deployment_id");
        asset.setCurrentDeploymentId(rs.wasNull() ? null : depId);

        asset.setCurrentLocation(rs.getString("current_location"));

        var purchaseDate = rs.getDate("purchase_date");
        asset.setPurchaseDate(purchaseDate != null ? purchaseDate.toLocalDate() : null);

        asset.setPurchaseCost(rs.getBigDecimal("purchase_cost"));
        asset.setQrCodePath(rs.getString("qr_code_path"));
        asset.setNotes(rs.getString("notes"));
        asset.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        asset.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return asset;
    }
}