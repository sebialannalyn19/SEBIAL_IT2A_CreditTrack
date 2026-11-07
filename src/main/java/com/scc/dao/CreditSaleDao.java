package com.scc.dao;

import com.scc.model.CreditSale;
import com.scc.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreditSaleDao {
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final CustomerDao customerDao = new CustomerDao();

    public void addCreditSale(int customerId, int userId, double amount, String description) throws SQLException {
        String sql = "INSERT INTO Credit_Sales (customer_id, user_id, amount, description, status) VALUES (?, ?, ?, ?, 'UNPAID')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, userId);
            stmt.setDouble(3, amount);
            stmt.setString(4, description);
            stmt.executeUpdate();
        }

        // Automatic Core Business Process Logging (Deliverable 8 Requirement)
        Customer cust = customerDao.getCustomerById(customerId);
        String customerName = (cust != null) ? cust.fullName() : ("ID #" + customerId);
        activityLogDao.logAction(userId, "Processed credit sale for Customer " + customerName + " - Total: ₱" + String.format("%.2f", amount));
    }

    public List<CreditSale> getAllCreditSales() throws SQLException {
        String sql = "SELECT sale_id, customer_id, user_id, amount, description, sale_date, status FROM Credit_Sales ORDER BY sale_id DESC";
        List<CreditSale> sales = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sales.add(new CreditSale(
                        rs.getInt("sale_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("user_id"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("sale_date"),
                        rs.getString("status")
                ));
            }
        }
        return sales;
    }
}