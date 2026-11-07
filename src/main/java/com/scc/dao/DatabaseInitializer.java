package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseInitializer {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            seedDefaultAdmin(conn);
            System.out.println("Database connection and admin verification initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        String sql = "INSERT OR IGNORE INTO Users(username, password_hash, role, status) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DEFAULT_ADMIN_USERNAME);
            ps.setString(2, DEFAULT_ADMIN_PASSWORD);
            ps.setString(3, "Admin");
            ps.setString(4, "APPROVED");
            ps.executeUpdate();
        }
    }
}