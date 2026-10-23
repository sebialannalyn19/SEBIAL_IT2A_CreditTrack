package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;

    // Shared instances are passed in (no new Scanner / UserDao here)
    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Choose option: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUser();
                case 4 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 1-4.");
            }
        }
    }

    private void viewAllUsers() {
        try {
            List<User> users = userDao.getAllUsers();
            System.out.println("\n=== ALL SYSTEM USERS ===");
            printUserTable(users);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewPendingUsers() {
        try {
            List<User> pending = userDao.getPendingUsers();
            System.out.println("\n=== PENDING REGISTRATIONS ===");
            printUserTable(pending);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void approveUser() {
        try {
            List<User> pending = userDao.getPendingUsers();
            System.out.println("\n=== PENDING REGISTRATIONS ===");
            printUserTable(pending);

            if (pending.isEmpty()) {
                return;
            }

            while (true) {
                System.out.print("Enter Target User ID to approve (0 to cancel): ");
                int targetId = readInt();

                if (targetId == 0) {
                    System.out.println("Approval cancelled.");
                    return;
                }

                // Verify the ID exists in the pending list
                boolean found = false;
                for (User u : pending) {
                    if (u.userId() == targetId) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.out.println("User ID " + targetId + " is not in the pending list. Try again.");
                    continue;
                }

                userDao.approveUser(targetId);
                System.out.println("Success! User ID " + targetId + " has been APPROVED.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // === Formatted Table Helper ===
    private void printUserTable(List<User> users) {
        String border = "+-------+-----------------+--------------+------------+";
        String format = "| %-5s | %-15s | %-12s | %-10s |%n";

        System.out.println(border);
        System.out.printf(format, "ID", "Username", "Role", "Status");
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf("| %-47s |%n", "No records found.");
        } else {
            for (User u : users) {
                System.out.printf(format, u.userId(), u.username(), u.role(), u.status());
            }
        }
        System.out.println(border);
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
}