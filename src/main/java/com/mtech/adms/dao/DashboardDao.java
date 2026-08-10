package com.mtech.adms.dao;

import com.mtech.adms.exception.DataAccessException;
import com.mtech.adms.model.DashboardStats;
import com.mtech.adms.model.RecentDeploymentSummary;
import com.mtech.adms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs aggregate queries across multiple tables to build the
 * Dashboard's summary statistics and recent activity feed.
 * See Phase 8 notes: intentionally not a GenericDao, since it
 * doesn't represent CRUD operations on a single entity.
 */
public class DashboardDao {

    /**
     * Builds the full set of dashboard stat counts in one pass.
     * Each count is a separate query for clarity and to keep the
     * SQL simple/readable - this table is not large enough for
     * the extra query count to matter performance-wise.
     */
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        try (Connection conn = DBConnection.getConnection()) {

            stats.setTotalAssets(countAssets(conn, null));
            stats.setAvailableAssets(countAssets(conn, "AVAILABLE"));
            stats.setDeployedAssets(countAssets(conn, "DEPLOYED"));
            stats.setMissingAssets(countAssets(conn, "MISSING"));
            stats.setDamagedAssets(countAssets(conn, "DAMAGED"));
            stats.setUnderRepairAssets(countAssets(conn, "UNDER_REPAIR"));

            stats.setActiveProjects(countRows(conn,
                    "SELECT COUNT(*) FROM projects WHERE status = 'ACTIVE' AND is_active = TRUE"));

            stats.setActiveSites(countRows(conn,
                    "SELECT COUNT(*) FROM sites WHERE is_active = TRUE"));

            stats.setActiveEmployees(countRows(conn,
                    "SELECT COUNT(*) FROM employees WHERE is_active = TRUE"));

            stats.setOverdueReturns(countRows(conn,
                    "SELECT COUNT(*) FROM deployments " +
                            "WHERE status IN ('ACTIVE','PARTIALLY_RETURNED') " +
                            "AND expected_return_date < CURDATE()"));

            return stats;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to load dashboard statistics", e);
        }
    }

    private int countAssets(Connection conn, String status) throws SQLException {
        String sql = (status == null)
                ? "SELECT COUNT(*) FROM assets"
                : "SELECT COUNT(*) FROM assets WHERE status = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (status != null) {
                stmt.setString(1, status);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int countRows(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Returns the N most recently created deployments, regardless of status.
     */
    public List<RecentDeploymentSummary> getRecentDeployments(int limit) {
        String sql =
                "SELECT d.deployment_code, p.name AS project_name, s.site_name, " +
                        "e.full_name AS leader_name, d.deployment_date, d.expected_return_date, d.status " +
                        "FROM deployments d " +
                        "JOIN projects p ON d.project_id = p.id " +
                        "JOIN sites s ON d.site_id = s.id " +
                        "JOIN employees e ON d.team_leader_id = e.id " +
                        "ORDER BY d.created_at DESC " +
                        "LIMIT ?";

        return queryDeploymentSummaries(sql, limit);
    }

    /**
     * Returns active/partially-returned deployments whose expected
     * return date has already passed - used for the Overdue Returns alert panel.
     */
    public List<RecentDeploymentSummary> getOverdueDeployments(int limit) {
        String sql =
                "SELECT d.deployment_code, p.name AS project_name, s.site_name, " +
                        "e.full_name AS leader_name, d.deployment_date, d.expected_return_date, d.status " +
                        "FROM deployments d " +
                        "JOIN projects p ON d.project_id = p.id " +
                        "JOIN sites s ON d.site_id = s.id " +
                        "JOIN employees e ON d.team_leader_id = e.id " +
                        "WHERE d.status IN ('ACTIVE','PARTIALLY_RETURNED') " +
                        "AND d.expected_return_date < CURDATE() " +
                        "ORDER BY d.expected_return_date ASC " +
                        "LIMIT ?";

        return queryDeploymentSummaries(sql, limit);
    }

    private List<RecentDeploymentSummary> queryDeploymentSummaries(String sql, int limit) {
        List<RecentDeploymentSummary> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecentDeploymentSummary summary = new RecentDeploymentSummary();
                    summary.setDeploymentCode(rs.getString("deployment_code"));
                    summary.setProjectName(rs.getString("project_name"));
                    summary.setSiteName(rs.getString("site_name"));
                    summary.setTeamLeaderName(rs.getString("leader_name"));
                    summary.setDeploymentDate(rs.getDate("deployment_date").toLocalDate());
                    summary.setExpectedReturnDate(rs.getDate("expected_return_date").toLocalDate());
                    summary.setStatus(rs.getString("status"));
                    results.add(summary);
                }
            }
            return results;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to load deployment summaries", e);
        }
    }
}