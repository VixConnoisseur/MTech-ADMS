package com.mtech.adms.dao;

import com.mtech.adms.exception.DataAccessException;
import com.mtech.adms.model.AssetMovement;
import com.mtech.adms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Records and retrieves asset movement history. Insert is also
 * exposed to accept an existing Connection, so AssetDao can log a
 * movement as part of the same transaction as a status update -
 * ensuring the asset's status and its audit trail never get out
 * of sync even if something fails partway through.
 */
public class AssetMovementDao {

    /**
     * Inserts a movement record using the caller's existing connection
     * and transaction. Does NOT commit - the caller controls that.
     */
    public void insert(Connection conn, AssetMovement movement) throws SQLException {
        String sql = "INSERT INTO asset_movements " +
                "(asset_id, deployment_id, from_status, to_status, from_location, to_location, moved_by, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movement.getAssetId());
            if (movement.getDeploymentId() != null) {
                stmt.setInt(2, movement.getDeploymentId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, movement.getFromStatus());
            stmt.setString(4, movement.getToStatus());
            stmt.setString(5, movement.getFromLocation());
            stmt.setString(6, movement.getToLocation());
            stmt.setInt(7, movement.getMovedBy());
            stmt.setString(8, movement.getNotes());

            stmt.executeUpdate();
        }
    }

    /**
     * Returns the full movement history for one asset, most recent first.
     * Used by the Asset Movement History screen (Phase 17) and could be
     * shown on an asset detail view later.
     */
    public List<AssetMovement> findByAssetId(int assetId) {
        String sql =
                "SELECT m.id, m.asset_id, m.deployment_id, m.from_status, m.to_status, " +
                        "m.from_location, m.to_location, m.moved_by, u.full_name AS moved_by_name, " +
                        "m.notes, m.moved_at " +
                        "FROM asset_movements m " +
                        "JOIN users u ON m.moved_by = u.id " +
                        "WHERE m.asset_id = ? " +
                        "ORDER BY m.moved_at DESC";

        List<AssetMovement> movements = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assetId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AssetMovement m = new AssetMovement();
                    m.setId(rs.getInt("id"));
                    m.setAssetId(rs.getInt("asset_id"));

                    int depId = rs.getInt("deployment_id");
                    m.setDeploymentId(rs.wasNull() ? null : depId);

                    m.setFromStatus(rs.getString("from_status"));
                    m.setToStatus(rs.getString("to_status"));
                    m.setFromLocation(rs.getString("from_location"));
                    m.setToLocation(rs.getString("to_location"));
                    m.setMovedBy(rs.getInt("moved_by"));
                    m.setMovedByName(rs.getString("moved_by_name"));
                    m.setNotes(rs.getString("notes"));
                    m.setMovedAt(rs.getTimestamp("moved_at").toLocalDateTime());

                    movements.add(m);
                }
            }
            return movements;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch movement history for asset id: " + assetId, e);
        }
    }
}