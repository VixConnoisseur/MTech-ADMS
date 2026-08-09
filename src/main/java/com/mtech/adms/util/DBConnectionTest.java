package com.mtech.adms.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Temporary manual test to verify DBConnection works correctly.
 * Not a unit test framework class - just a runnable main() for Phase 4 verification.
 * Safe to delete once Phase 4 is confirmed working.
 */
public class DBConnectionTest {

    public static void main(String[] args) {
        System.out.println("Attempting to connect to mtech_adms...");

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS: Connected to database.");
                System.out.println("Database product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Database version: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("Connected as: " + conn.getMetaData().getUserName());
            }
        } catch (SQLException e) {
            System.out.println("FAILED to connect.");
            e.printStackTrace();
        }
    }
}