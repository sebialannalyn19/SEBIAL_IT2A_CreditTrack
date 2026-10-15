package com.scc.dao;

import com.scc.model.User;
import java.sql.*;

public class UserDao {
    public void register(User user) throws SQLException {
        // Prevent duplicate usernames
        String checkSql = "SELECT COUNT(*) FROM Users WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, user.username());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Username already exists.");
            }
        }

        String sql = "INSERT INTO Users(username, password_hash, role, status) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.passwordHash());
            stmt.setString(3, user.role());
            stmt.setString(4, user.status());
            stmt.executeUpdate();
        }
    }

    public User login(String username, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM Users WHERE username=? AND password_hash=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("status")
                );
            }
        }
        return null;
    }

    public void approveUser(int userId) throws SQLException {
        String sql = "UPDATE Users SET status='APPROVED' WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public void listPendingUsers() throws SQLException {
        String sql = "SELECT user_id, username, role, status FROM Users WHERE status='PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("=== Pending Users ===");
            while (rs.next()) {
                System.out.printf("ID: %d | Username: %s | Role: %s | Status: %s%n",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("status"));
            }
        }
    }
}
