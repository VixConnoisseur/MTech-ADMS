package com.mtech.adms.dao;

import com.mtech.adms.exception.DataAccessException;
import com.mtech.adms.model.Project;
import com.mtech.adms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDao implements GenericDao<Project, Integer> {

    private static final String BASE_SELECT =
            "SELECT id, project_code, name, client_name, start_date, end_date, " +
                    "status, is_active, created_at, updated_at FROM projects ";

    @Override
    public Project insert(Project project) {
        // Kept for GenericDao compliance, but project creation always
        // needs to also write project_sites - use insertWithSites() instead.
        throw new UnsupportedOperationException(
                "Use insertWithSites() so the project and its site links are saved in one transaction.");
    }

    @Override
    public boolean update(Project project) {
        throw new UnsupportedOperationException(
                "Use updateWithSites() so the project and its site links are saved in one transaction.");
    }

    /**
     * Inserts a project and its linked sites (project_sites) as a single
     * transaction. If either step fails, both are rolled back so we never
     * end up with a project that has no valid site links, or vice versa.
     */
    public Project insertWithSites(Project project) {
        String insertProjectSql =
                "INSERT INTO projects (project_code, name, client_name, start_date, end_date, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        insertProjectSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, project.getProjectCode());
                    stmt.setString(2, project.getName());
                    stmt.setString(3, project.getClientName());
                    stmt.setObject(4, project.getStartDate());
                    stmt.setObject(5, project.getEndDate());
                    stmt.setString(6, project.getStatus());

                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            project.setId(keys.getInt(1));
                        }
                    }
                }

                insertProjectSites(conn, project.getId(), project.getSiteIds());

                conn.commit();
                return project;

            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed to create project: " + project.getName(), e);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create project: " + project.getName(), e);
        }
    }

    /**
     * Updates a project and replaces its project_sites links entirely
     * (delete old links, insert the new selection) in one transaction.
     */
    public boolean updateWithSites(Project project) {
        String updateProjectSql =
                "UPDATE projects SET name = ?, client_name = ?, start_date = ?, end_date = ?, " +
                        "status = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt = conn.prepareStatement(updateProjectSql)) {
                    stmt.setString(1, project.getName());
                    stmt.setString(2, project.getClientName());
                    stmt.setObject(3, project.getStartDate());
                    stmt.setObject(4, project.getEndDate());
                    stmt.setString(5, project.getStatus());
                    stmt.setBoolean(6, project.isActive());
                    stmt.setInt(7, project.getId());
                    stmt.executeUpdate();
                }

                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM project_sites WHERE project_id = ?")) {
                    del.setInt(1, project.getId());
                    del.executeUpdate();
                }

                insertProjectSites(conn, project.getId(), project.getSiteIds());

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Failed to update project id: " + project.getId(), e);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update project id: " + project.getId(), e);
        }
    }

    private void insertProjectSites(Connection conn, int projectId, List<Integer> siteIds) throws SQLException {
        if (siteIds == null || siteIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO project_sites (project_id, site_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer siteId : siteIds) {
                stmt.setInt(1, projectId);
                stmt.setInt(2, siteId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public Optional<Project> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Project project = mapRow(rs);
                    project.setSiteIds(findSiteIdsForProject(conn, id));
                    return Optional.of(project);
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find project by id: " + id, e);
        }
    }

    @Override
    public List<Project> findAll() {
        return queryList(BASE_SELECT + "ORDER BY created_at DESC", null);
    }

    public List<Project> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = BASE_SELECT + "WHERE name LIKE ? OR project_code LIKE ? ORDER BY created_at DESC";
        String pattern = "%" + keyword.trim() + "%";
        return queryList(sql, new String[]{pattern, pattern});
    }

    private List<Project> queryList(String sql, String[] params) {
        List<Project> projects = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapRow(rs));
                }
            }
            return projects;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch projects", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        throw new UnsupportedOperationException(
                "Projects are not hard-deleted. Use updateWithSites() with is_active=false instead.");
    }

    public String getNextProjectCode() {
        String sql = "SELECT project_code FROM projects WHERE project_code LIKE 'PRJ-%' " +
                "ORDER BY id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int nextNumber = 1;
            if (rs.next()) {
                String lastCode = rs.getString("project_code");
                String numericPart = lastCode.substring(lastCode.lastIndexOf('-') + 1);
                nextNumber = Integer.parseInt(numericPart) + 1;
            }
            return String.format("PRJ-%03d", nextNumber);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate next project code", e);
        }
    }

    private List<Integer> findSiteIdsForProject(Connection conn, int projectId) throws SQLException {
        List<Integer> siteIds = new ArrayList<>();
        String sql = "SELECT site_id FROM project_sites WHERE project_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    siteIds.add(rs.getInt("site_id"));
                }
            }
        }
        return siteIds;
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setProjectCode(rs.getString("project_code"));
        project.setName(rs.getString("name"));
        project.setClientName(rs.getString("client_name"));

        var start = rs.getDate("start_date");
        project.setStartDate(start != null ? start.toLocalDate() : null);

        var end = rs.getDate("end_date");
        project.setEndDate(end != null ? end.toLocalDate() : null);

        project.setStatus(rs.getString("status"));
        project.setActive(rs.getBoolean("is_active"));
        project.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        project.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return project;
    }
}