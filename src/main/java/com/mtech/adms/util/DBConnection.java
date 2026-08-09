package com.mtech.adms.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central utility for obtaining a JDBC connection to the mtech_adms database.
 * Reads connection details from config.properties on the classpath so that
 * credentials never appear in source code.
 *
 * Usage:
 *   try (Connection conn = DBConnection.getConnection()) {
 *       // use conn
 *   }
 */
public final class DBConnection {

    private static final String CONFIG_FILE = "/config.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfig();
    }

    // Prevent instantiation - this is a static utility class.
    private DBConnection() {
    }

    private static void loadConfig() {
        Properties props = new Properties();

        try (InputStream input = DBConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException(
                        "config.properties not found on classpath. " +
                                "Copy config.properties.example to config.properties " +
                                "and fill in your database credentials."
                );
            }
            props.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read config.properties", e);
        }

        String host = props.getProperty("db.host");
        String port = props.getProperty("db.port");
        String dbName = props.getProperty("db.name");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");

        if (host == null || port == null || dbName == null || user == null || password == null) {
            throw new IllegalStateException(
                    "config.properties is missing one or more required keys: " +
                            "db.host, db.port, db.name, db.user, db.password"
            );
        }

        url = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                host, port, dbName
        );
    }

    /**
     * Opens a new JDBC connection to the mtech_adms database.
     * Caller is responsible for closing it (use try-with-resources).
     *
     * @return an open Connection
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}