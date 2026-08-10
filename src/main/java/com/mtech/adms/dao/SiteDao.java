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

/**
 * Minimal DAO for Phase 10 - only findAll() is needed to populate
 * the site multi-select in the Project form. Phase 11 (Site
 * Management) will add insert/update/search here, matching the
 * EmployeeDao pattern.
 */
public class SiteDao {

    public List<Site> findAll() {
        String sql = "SELECT id, site_name, address, city, is_active " +
                "FROM sites WHERE is_active = TRUE ORDER BY site_name";

        List<Site> sites = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Site site = new Site();
                site.setId(rs.getInt("id"));
                site.setSiteName(rs.getString("site_name"));
                site.setAddress(rs.getString("address"));
                site.setCity(rs.getString("city"));
                site.setActive(rs.getBoolean("is_active"));
                sites.add(site);
            }
            return sites;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch sites", e);
        }
    }
}