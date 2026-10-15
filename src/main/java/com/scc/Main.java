package com.scc;

import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.ui.AdminMenu;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(String[] args) {
        while (true) {
            System.out.println("=== CreditTrack Authentication ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 3 -> { System.out.println("Exiting system... Goodbye!"); return; }
                default -> System.out.println("Invalid choice. Please enter 1–3.");
            }
        }
    }

    private static void register() {
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

    private static void login() {
        String username = readNonEmpty("Username: ");
        String password = readNonEmpty("Password: ");

        try {
            User user = userDao.login(username, password);
            if (user != null) {
                if (!user.status().equalsIgnoreCase("APPROVED")) {
                    System.out.println("Your account is still pending approval by the Admin.");
                    return;
                }
                System.out.println("Welcome, " + user.username() + " (" + user.role() + ")");
                if (user.role().equalsIgnoreCase("Admin")) {
                    new AdminMenu().start();
                } else if (user.role().equalsIgnoreCase("Cashier")) {
                    System.out.println("You can record credit sales and payments.");
                } else if (user.role().equalsIgnoreCase("Customer")) {
                    System.out.println("You can view your credit history and balance.");
                }
            } else {
                System.out.println("Invalid credentials. Try again.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // === Validation Helpers ===
    private static String readNonEmpty(String prompt) {
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

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Please enter again: ");
            }
        }
    }

    private static String readRole() {
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
                default -> System.out.println("Invalid role choice. Please select 1–3.");
            }
        }
    }
}
