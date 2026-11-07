package com.scc.dao;

import com.scc.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public void register(User user) throws SQLException {
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
            stmt.setString(3, user.role().toUpperCase());
            stmt.setString(4, user.status().toUpperCase());
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

    public void approveUser(int targetUserId, int adminUserId) throws SQLException {
        String sql = "UPDATE Users SET status = 'APPROVED' WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, targetUserId);
            stmt.executeUpdate();
        }
        activityLogDao.logAction(adminUserId, "Admin [ID: " + adminUserId + "] approved account for User ID " + targetUserId);
    }

    public void updateUserRoleAndStatus(int targetUserId, String newRole, String newStatus, int adminUserId) throws SQLException {
        String sql = "UPDATE Users SET role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole.toUpperCase());
            stmt.setString(2, newStatus.toUpperCase());
            stmt.setInt(3, targetUserId);
            stmt.executeUpdate();
        }
        activityLogDao.logAction(adminUserId, "Admin [ID: " + adminUserId + "] changed role/status of User " + targetUserId + " to " + newRole.toUpperCase() + "/" + newStatus.toUpperCase());
    }

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