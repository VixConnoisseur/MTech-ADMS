package com.mtech.adms.dao;

import com.mtech.adms.exception.DataAccessException;
import com.mtech.adms.model.User;
import com.mtech.adms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the users table. Joins against roles to include
 * the role name, since callers almost always need it alongside the user.
 */
public class UserDao implements GenericDao<User, Integer> {

    private static final String BASE_SELECT =
            "SELECT u.id, u.role_id, r.name AS role_name, u.username, " +
                    "u.password_hash, u.full_name, u.email, u.is_active, " +
                    "u.last_login_at, u.created_at, u.updated_at " +
                    "FROM users u JOIN roles r ON u.role_id = r.id ";

    @Override
    public User insert(User user) {
        String sql = "INSERT INTO users (role_id, username, password_hash, full_name, email) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, user.getRoleId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating user failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert user: " + user.getUsername(), e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET role_id = ?, username = ?, full_name = ?, " +
                "email = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getRoleId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user id: " + user.getId(), e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = BASE_SELECT + "WHERE u.id = ?";

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
            throw new DataAccessException("Failed to find user by id: " + id, e);
        }
    }

    /**
     * Finds a user by username. Used specifically during login.
     */
    public Optional<User> findByUsername(String username) {
        String sql = BASE_SELECT + "WHERE u.username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user by username: " + username, e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = BASE_SELECT + "ORDER BY u.full_name";
        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch users", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        // Users are soft-deleted via is_active, not hard-deleted.
        // See update() - flip is_active to false instead of calling this.
        throw new UnsupportedOperationException(
                "Users are not hard-deleted. Use update() with is_active=false instead.");
    }

    /**
     * Updates the last_login_at timestamp to now. Called after a
     * successful login.
     */
    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login_at = NOW() WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update last login for user id: " + userId, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setRoleId(rs.getInt("role_id"));
        user.setRoleName(rs.getString("role_name"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));

        Timestamp lastLogin = rs.getTimestamp("last_login_at");
        if (lastLogin != null) {
            user.setLastLoginAt(lastLogin.toLocalDateTime());
        }
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return user;
    }
}