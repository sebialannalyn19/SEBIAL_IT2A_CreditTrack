package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public void logAction(int userId, String action) throws SQLException {
        String sql = "INSERT INTO Activity_Logs (user_id, action) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.executeUpdate();
        }
    }

    public List<ActivityLog> getAllLogs() throws SQLException {
        String sql = "SELECT log_id, user_id, action, log_time FROM Activity_Logs ORDER BY log_time DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                logs.add(new ActivityLog(
                        rs.getInt("log_id"),
                        rs.getInt("user_id"),
                        rs.getString("action"),
                        rs.getString("log_time")
                ));
            }
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) throws SQLException {
        String sql = "SELECT log_id, user_id, action, log_time FROM Activity_Logs WHERE user_id = ? ORDER BY log_time DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(new ActivityLog(
                            rs.getInt("log_id"),
                            rs.getInt("user_id"),
                            rs.getString("action"),
                            rs.getString("log_time")
                    ));
                }
            }
        }
        return logs;
    }
}