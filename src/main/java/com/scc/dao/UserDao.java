package com.scc.dao;

import com.scc.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                return mapUser(rs);
            }
        }
        return null;
    }

    /** Returns all registered accounts regardless of role. */
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, status FROM Users ORDER BY user_id";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    /** Returns all accounts with status = 'PENDING' across every role. */
    public List<User> getPendingUsers() throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, status FROM Users WHERE status = ? ORDER BY user_id";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "PENDING");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        return users;
    }

    /** Updates a user's status from PENDING to APPROVED. */
    public void approveUser(int userId) throws SQLException {
        String sql = "UPDATE Users SET status = 'APPROVED' WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    // Converts one database row into a User record
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("status")
        );
    }
}