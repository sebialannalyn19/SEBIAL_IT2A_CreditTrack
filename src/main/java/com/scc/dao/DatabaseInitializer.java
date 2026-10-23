package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ensures all required tables exist before the CLI menu starts.
 */
public class DatabaseInitializer {

    public static void initialize() {
        String users = """
                CREATE TABLE IF NOT EXISTS Users (
                    user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username      TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role          TEXT NOT NULL CHECK (role IN ('Admin','Cashier','Customer')),
                    status        TEXT NOT NULL DEFAULT 'PENDING'
                );
                """;

        String customers = """
                CREATE TABLE IF NOT EXISTS Customers (
                    customer_id  INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id      INTEGER,
                    full_name    TEXT NOT NULL,
                    contact_no   TEXT,
                    address      TEXT,
                    credit_limit REAL NOT NULL DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                );
                """;

        String creditSales = """
                CREATE TABLE IF NOT EXISTS Credit_Sales (
                    sale_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    user_id     INTEGER NOT NULL,
                    amount      REAL NOT NULL,
                    description TEXT,
                    sale_date   TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status      TEXT NOT NULL DEFAULT 'UNPAID',
                    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
                    FOREIGN KEY (user_id)     REFERENCES Users(user_id)
                );
                """;

        String payments = """
                CREATE TABLE IF NOT EXISTS Payments (
                    payment_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id      INTEGER NOT NULL,
                    user_id      INTEGER NOT NULL,
                    amount       REAL NOT NULL,
                    payment_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (sale_id) REFERENCES Credit_Sales(sale_id),
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                );
                """;

        String activityLogs = """
                CREATE TABLE IF NOT EXISTS Activity_Logs (
                    log_id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id   INTEGER NOT NULL,
                    action    TEXT NOT NULL,
                    log_time  TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES Users(user_id)
                );
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(users);
            stmt.execute(customers);
            stmt.execute(creditSales);
            stmt.execute(payments);
            stmt.execute(activityLogs);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }
}