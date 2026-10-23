package com.scc.ui;

import com.scc.model.User;
import java.util.Scanner;

public class CashierMenu {
    private final Scanner scanner;

    public CashierMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void start(User user) {
        while (true) {
            System.out.println("\n=== CASHIER MENU (" + user.username() + ") ===");
            System.out.println("1. Record Credit Sale (coming soon)");
            System.out.println("2. Record Payment (coming soon)");
            System.out.println("3. Logout");
            System.out.print("Choose option: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1", "2" -> System.out.println("This feature will be added in a later deliverable.");
                case "3" -> { System.out.println("Logging out..."); return; }
                default -> System.out.println("Invalid choice. Please enter 1-3.");
            }
        }
    }
}