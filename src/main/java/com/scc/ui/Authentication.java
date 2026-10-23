package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

/**
 * Handles registration, login, and role-based routing.
 * Receives shared UserDao and Scanner instances (no duplicates created).
 */
public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== CreditTrack Authentication ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> {
                    System.out.println("Exiting system... Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 1-3.");
            }
        }
    }

    private void register() {
        String username = readNonEmpty("Username: ");
        String password = readNonEmpty("Password: ");
        String role = readRole();

        try {
            userDao.register(new User(0, username, password, role, "PENDING"));
            System.out.println("User registered successfully! Status: PENDING approval.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void login() {
        String username = readNonEmpty("Username: ");
        String password = readNonEmpty("Password: ");

        try {
            User user = userDao.login(username, password);
            if (user == null) {
                System.out.println("Invalid credentials. Try again.");
                return;
            }
            if (!user.status().equalsIgnoreCase("APPROVED")) {
                System.out.println("Your account is still pending approval by the Admin.");
                return;
            }
            System.out.println("Welcome, " + user.username() + " (" + user.role() + ")");
            routeByRole(user);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void routeByRole(User user) {
        switch (user.role().toLowerCase()) {
            case "admin" -> new AdminMenu(userDao, scanner).start();
            case "cashier" -> new CashierMenu(scanner).start(user);
            case "customer" -> new CustomerMenu(scanner).start(user);
            default -> System.out.println("Unknown role: " + user.role());
        }
    }

    // === Validation Helpers ===
    private String readNonEmpty(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        } while (input.isEmpty());
        return input;
    }

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Please enter again: ");
            }
        }
    }

    private String readRole() {
        while (true) {
            System.out.println("Select Role:");
            System.out.println("1. Admin / Store Owner");
            System.out.println("2. Cashier / Staff");
            System.out.println("3. Customer");
            System.out.print("Choice: ");
            int choice = readInt();
            switch (choice) {
                case 1 -> { return "Admin"; }
                case 2 -> { return "Cashier"; }
                case 3 -> { return "Customer"; }
                default -> System.out.println("Invalid role choice. Please select 1-3.");
            }
        }
    }
}