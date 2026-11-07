package com.scc.dao;

import com.scc.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public void addCustomer(String fullName, String contactNo, String address, double creditLimit, Integer userId) throws SQLException {
        String sql = "INSERT INTO Customers (user_id, full_name, contact_no, address, credit_limit) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, fullName);
            stmt.setString(3, contactNo);
            stmt.setString(4, address);
            stmt.setDouble(5, creditLimit);
            stmt.executeUpdate();
        }
    }

    public List<Customer> getAllCustomers() throws SQLException {
        String sql = "SELECT customer_id, user_id, full_name, contact_no, address, credit_limit FROM Customers ORDER BY customer_id";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                        rs.getString("full_name"),
                        rs.getString("contact_no"),
                        rs.getString("address"),
                        rs.getDouble("credit_limit")
                ));
            }
        }
        return customers;
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, user_id, full_name, contact_no, address, credit_limit FROM Customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                            rs.getString("full_name"),
                            rs.getString("contact_no"),
                            rs.getString("address"),
                            rs.getDouble("credit_limit")
                    );
                }
            }
        }
        return null;
    }
}