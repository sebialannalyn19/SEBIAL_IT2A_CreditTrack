package com.scc.ui;

import com.scc.dao.UserDao;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner;
    private final UserDao userDao;

    // Shared instances are passed in (no new Scanner / UserDao here)
    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View Pending Users");
            System.out.println("2. Approve User");
            System.out.println("3. Logout");
            System.out.print("Choose option: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> viewPending();
                case 2 -> approveUser();
                case 3 -> { System.out.println("Logging out..."); return; }
                default -> System.out.println("Invalid choice. Please enter 1-3.");
            }
        }
    }

    private void viewPending() {
        try {
            userDao.listPendingUsers();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void approveUser() {
        System.out.print("Enter User ID to approve: ");
        int userId = readInt();
        try {
            userDao.approveUser(userId);
            System.out.println("User approved successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
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