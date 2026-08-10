package com.mtech.adms.dao;

import com.mtech.adms.exception.DataAccessException;
import com.mtech.adms.model.Employee;
import com.mtech.adms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDao implements GenericDao<Employee, Integer> {

    private static final String BASE_SELECT =
            "SELECT id, user_id, employee_code, full_name, contact_no, position, " +
                    "is_active, created_at, updated_at FROM employees ";

    @Override
    public Employee insert(Employee employee) {
        String sql = "INSERT INTO employees (employee_code, full_name, contact_no, position) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, employee.getEmployeeCode());
            stmt.setString(2, employee.getFullName());
            stmt.setString(3, employee.getContactNo());
            stmt.setString(4, employee.getPosition());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating employee failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    employee.setId(keys.getInt(1));
                }
            }
            return employee;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert employee: " + employee.getFullName(), e);
        }
    }

    @Override
    public boolean update(Employee employee) {
        String sql = "UPDATE employees SET full_name = ?, contact_no = ?, position = ?, " +
                "is_active = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getFullName());
            stmt.setString(2, employee.getContactNo());
            stmt.setString(3, employee.getPosition());
            stmt.setBoolean(4, employee.isActive());
            stmt.setInt(5, employee.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update employee id: " + employee.getId(), e);
        }
    }

    @Override
    public Optional<Employee> findById(Integer id) {
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
            throw new DataAccessException("Failed to find employee by id: " + id, e);
        }
    }

    @Override
    public List<Employee> findAll() {
        String sql = BASE_SELECT + "ORDER BY full_name";
        return queryList(sql, null);
    }

    public List<Employee> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = BASE_SELECT + "WHERE full_name LIKE ? OR employee_code LIKE ? ORDER BY full_name";
        String pattern = "%" + keyword.trim() + "%";
        return queryList(sql, new String[]{pattern, pattern});
    }

    private List<Employee> queryList(String sql, String[] params) {
        List<Employee> employees = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapRow(rs));
                }
            }
            return employees;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch employees", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        throw new UnsupportedOperationException(
                "Employees are not hard-deleted. Use update() with is_active=false instead.");
    }

    public String getNextEmployeeCode() {
        String sql = "SELECT employee_code FROM employees WHERE employee_code LIKE 'EMP-%' " +
                "ORDER BY id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int nextNumber = 1;
            if (rs.next()) {
                String lastCode = rs.getString("employee_code");
                String numericPart = lastCode.substring(lastCode.lastIndexOf('-') + 1);
                nextNumber = Integer.parseInt(numericPart) + 1;
            }
            return String.format("EMP-%05d", nextNumber);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate next employee code", e);
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));

        int userId = rs.getInt("user_id");
        employee.setUserId(rs.wasNull() ? null : userId);

        employee.setEmployeeCode(rs.getString("employee_code"));
        employee.setFullName(rs.getString("full_name"));
        employee.setContactNo(rs.getString("contact_no"));
        employee.setPosition(rs.getString("position"));
        employee.setActive(rs.getBoolean("is_active"));
        employee.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        employee.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return employee;
    }
}