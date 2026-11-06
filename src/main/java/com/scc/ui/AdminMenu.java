package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, Scanner scanner, User currentAdmin) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    public void start() {
        while (true) {
            System.out.println("\n=================================================");
            System.out.println("                 ADMIN DASHBOARD                 ");
            System.out.println("=================================================");
            System.out.println("Logged in as: " + currentAdmin.username() + " (" + currentAdmin.role() + ")");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Update User");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            int choice = readIntInput();
            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUser();
                case 4 -> updateUser();
                case 5 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 1-5.");
            }
        }
    }

    private void viewAllUsers() {
        try {
            List<User> users = userDao.getAllUsers();
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
            printUserTable(pending);

            if (pending.isEmpty()) {
                return;
            }

            while (true) {
                System.out.print("Enter Target User ID to approve (Enter 0 to cancel): ");
                int targetId = readIntInput();

                if (targetId == 0) {
                    System.out.println("Approval cancelled.");
                    return;
                }

                boolean found = false;
                for (User u : pending) {
                    if (u.userId() == targetId) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.out.println("Error: User ID " + targetId + " is not in the pending list. Try again.");
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

    private void updateUser() {
        try {
            List<User> users = userDao.getAllUsers();
            printUserTable(users);

            if (users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }

            int targetId;
            User targetUser = null;

            while (true) {
                System.out.print("Enter User ID to update (Enter 0 to cancel): ");
                targetId = readIntInput();

                if (targetId == 0) {
                    System.out.println("Update cancelled.");
                    return;
                }

                // Security Rule: Prevent modifying active own account
                if (targetId == currentAdmin.userId()) {
                    System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                    continue;
                }

                for (User u : users) {
                    if (u.userId() == targetId) {
                        targetUser = u;
                        break;
                    }
                }

                if (targetUser == null) {
                    System.out.println("Error: User ID not found.");
                    continue;
                }
                break;
            }

            String newRole = readValidRole();
            if (newRole.equals("0")) {
                System.out.println("Update cancelled.");
                return;
            }

            String newStatus = readValidStatus();
            if (newStatus.equals("0")) {
                System.out.println("Update cancelled.");
                return;
            }

            userDao.updateUserRoleAndStatus(targetId, newRole, newStatus);
            System.out.println("-> User role and status updated successfully!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // === Formatted Table Helper ===
    private void printUserTable(List<User> users) {
        String border = "+-------+-----------------+---------------------+------------+";
        String format = "| %-5s | %-15s | %-19s | %-10s |%n";

        System.out.println("\n--- SELECT USER TO UPDATE ---");
        System.out.println(border);
        System.out.printf(format, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf("| %-49s |%n", "No records found.");
        } else {
            for (User u : users) {
                System.out.printf(format, u.userId(), u.username(), u.role(), u.status());
            }
        }
        System.out.println(border);
    }

    // === Validation Wrapper Helpers ===
    private int readIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid integer number.\nEnter User ID to update (Enter 0 to cancel): ");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("Select Role:");
            System.out.println("[1] ADMIN");
            System.out.println("[2] CASHIER");
            System.out.println("[3] CUSTOMER");
            System.out.print("Role choice (Enter 0 to cancel): ");

            String input = scanner.nextLine().trim();
            if (input.equals("0")) return "0";

            switch (input) {
                case "1" -> { return "Admin"; }
                case "2" -> { return "Cashier"; }
                case "3" -> { return "Customer"; }
                default -> System.out.println("Invalid role choice. Please select 1-3 or 0 to cancel.");
            }
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter status (APPROVED / PENDING) (Enter 0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) return "0";
            if (input.equals("APPROVED") || input.equals("PENDING")) {
                return input;
            }
            System.out.println("Invalid status. Please enter either APPROVED or PENDING.");
        }
    }
}