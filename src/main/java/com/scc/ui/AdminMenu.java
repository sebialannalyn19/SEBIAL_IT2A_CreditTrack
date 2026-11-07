package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.CreditSaleDao;
import com.scc.dao.CustomerDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.CreditSale;
import com.scc.model.Customer;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentAdmin;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final CreditSaleDao creditSaleDao = new CreditSaleDao();
    private final CustomerDao customerDao = new CustomerDao();

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
            System.out.println("4. Manage / Add Customers");
            System.out.println("5. Update User");
            System.out.println("6. Delete User (Coming soon)");
            System.out.println("7. View All Credit Sales / Add Sale");
            System.out.println("8. View System Audit Logs");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");

            int choice = readIntInput();
            switch (choice) {
                case 1 -> viewAllUsers();
                case 2 -> viewPendingUsers();
                case 3 -> approveUser();
                case 4 -> manageCustomers();
                case 5 -> updateUser();
                case 6 -> System.out.println("This feature is part of upcoming deliverable modules.");
                case 7 -> viewAndAddCreditSales();
                case 8 -> viewSystemAuditLogs();
                case 9 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 1-9.");
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

                userDao.approveUser(targetId, currentAdmin.userId());
                System.out.println("-> Success! User ID " + targetId + " has been APPROVED.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manageCustomers() {
        while (true) {
            System.out.println("\n=================================================");
            System.out.println("               CUSTOMER MANAGEMENT               ");
            System.out.println("=================================================");
            System.out.println("1. View All Customers");
            System.out.println("2. Add New Customer");
            System.out.println("3. Back to Admin Dashboard");
            System.out.print("Choose option: ");

            int choice = readIntInput();
            try {
                switch (choice) {
                    case 1 -> {
                        List<Customer> customers = customerDao.getAllCustomers();
                        printCustomerTable(customers);
                    }
                    case 2 -> addNewCustomer();
                    case 3 -> { return; }
                    default -> System.out.println("Invalid choice. Please enter 1-3.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void addNewCustomer() {
        try {
            System.out.print("Enter Customer Full Name: ");
            String fullName = scanner.nextLine().trim();
            if (fullName.isEmpty()) {
                System.out.println("Name cannot be empty.");
                return;
            }

            System.out.print("Enter Contact Number: ");
            String contactNo = scanner.nextLine().trim();

            System.out.print("Enter Address: ");
            String address = scanner.nextLine().trim();

            System.out.print("Enter Credit Limit (₱): ");
            double creditLimit = readDoubleInput();

            customerDao.addCustomer(fullName, contactNo, address, creditLimit, null);
            activityLogDao.logAction(currentAdmin.userId(), "Admin added new customer: " + fullName);
            System.out.println("-> Success! Customer added successfully.");

        } catch (Exception e) {
            System.out.println("Error adding customer: " + e.getMessage());
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

            userDao.updateUserRoleAndStatus(targetId, newRole, newStatus, currentAdmin.userId());
            System.out.println("-> User role and status updated successfully!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewAndAddCreditSales() {
        while (true) {
            System.out.println("\n=================================================");
            System.out.println("             CREDIT SALES MANAGEMENT             ");
            System.out.println("=================================================");
            System.out.println("1. View All Credit Sales");
            System.out.println("2. Record New Credit Sale");
            System.out.println("3. Back to Admin Dashboard");
            System.out.print("Choose option: ");

            int choice = readIntInput();
            try {
                switch (choice) {
                    case 1 -> {
                        List<CreditSale> sales = creditSaleDao.getAllCreditSales();
                        printCreditSaleTable(sales);
                    }
                    case 2 -> recordNewCreditSale();
                    case 3 -> { return; }
                    default -> System.out.println("Invalid choice. Please enter 1-3.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void recordNewCreditSale() {
        try {
            List<Customer> customers = customerDao.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("No customers found in the database. Please go to Admin Menu -> Option 4 (Manage Customers) to add a customer first.");
                return;
            }

            printCustomerTable(customers);

            System.out.print("Enter Customer ID for this credit sale (Enter 0 to cancel): ");
            int customerId = readIntInput();
            if (customerId == 0) {
                System.out.println("Transaction cancelled.");
                return;
            }

            Customer selectedCustomer = customerDao.getCustomerById(customerId);
            if (selectedCustomer == null) {
                System.out.println("Error: Customer ID not found.");
                return;
            }

            System.out.print("Enter Item / Purchase Description: ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) {
                description = "General Store Credit";
            }

            System.out.print("Enter Credit Amount (₱): ");
            double amount = readDoubleInput();
            if (amount <= 0) {
                System.out.println("Error: Amount must be greater than zero.");
                return;
            }

            creditSaleDao.addCreditSale(customerId, currentAdmin.userId(), amount, description);
            System.out.println("-> Success! Credit sale recorded successfully and logged to audit trail.");

        } catch (Exception e) {
            System.out.println("Error recording credit sale: " + e.getMessage());
        }
    }

    private void viewSystemAuditLogs() {
        while (true) {
            System.out.println("\n=================================================");
            System.out.println("               SYSTEM AUDIT LOGS                 ");
            System.out.println("=================================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Dashboard");
            System.out.print("Choose option: ");

            int choice = readIntInput();
            try {
                switch (choice) {
                    case 1 -> {
                        List<ActivityLog> logs = activityLogDao.getAllLogs();
                        printAuditLogTable(logs);
                    }
                    case 2 -> {
                        System.out.print("Enter User ID to filter: ");
                        int uid = readIntInput();
                        List<ActivityLog> logs = activityLogDao.getLogsByUserId(uid);
                        printAuditLogTable(logs);
                    }
                    case 3 -> { return; }
                    default -> System.out.println("Invalid choice. Please enter 1-3.");
                }
            } catch (Exception e) {
                System.out.println("Error fetching logs: " + e.getMessage());
            }
        }
    }

    // === Extra Wide, Spotlessly Clean Box Table Formatters (Zero Overflow) ===

    private void printUserTable(List<User> users) {
        String border = "+---------+--------------------+----------------------+--------------+";
        String format = "| %-7s | %-18s | %-20s | %-12s |%n";

        System.out.println("\n" + border);
        System.out.printf(format, "USER ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf("| %-65s |%n", "No user records found.");
        } else {
            for (User u : users) {
                System.out.printf(format, u.userId(), u.username(), u.role(), u.status());
            }
        }
        System.out.println(border);
    }

    private void printCustomerTable(List<Customer> customers) {
        String border = "+-------------+----------------------------------------------+--------------------+----------------------------------------------+--------------+";
        String format = "| %-11s | %-44s | %-18s | %-44s | %-12s |%n";

        System.out.println("\n" + border);
        System.out.printf(format, "CUSTOMER ID", "FULL NAME", "CONTACT NO", "ADDRESS", "CREDIT LIMIT");
        System.out.println(border);

        if (customers.isEmpty()) {
            System.out.printf("| %-133s |%n", "No customer records found.");
        } else {
            for (Customer c : customers) {
                System.out.printf(format, c.customerId(), c.fullName(), c.contactNo(), c.address(), String.format("₱%.2f", c.creditLimit()));
            }
        }
        System.out.println(border);
    }

    private void printCreditSaleTable(List<CreditSale> sales) {
        String border = "+---------+-------------+---------+--------------+-----------------------------------------------------+---------------------+----------+";
        String format = "| %-7s | %-11s | %-7s | %-12s | %-51s | %-19s | %-8s |%n";

        System.out.println("\n" + border);
        System.out.printf(format, "SALE ID", "CUST ID", "USER ID", "AMOUNT", "DESCRIPTION / ITEMS", "SALE DATE", "STATUS");
        System.out.println(border);

        if (sales.isEmpty()) {
            System.out.printf("| %-127s |%n", "No credit sales found.");
        } else {
            for (CreditSale s : sales) {
                System.out.printf(format, s.saleId(), s.customerId(), s.userId(), String.format("₱%.2f", s.amount()), s.description(), s.saleDate(), s.status());
            }
        }
        System.out.println(border);
    }

    private void printAuditLogTable(List<ActivityLog> logs) {
        String border = "+---------+---------+--------------------+-----------------------------------------------------------------+----------------------+";
        String format = "| %-7s | %-7s | %-18s | %-65s | %-20s |%n";

        System.out.println("\n" + border);
        System.out.printf(format, "LOG ID", "USER ID", "ACTION TYPE", "DESCRIPTION / BUSINESS EVENT DETAIL", "TIMESTAMP");
        System.out.println(border);

        if (logs.isEmpty()) {
            System.out.printf("| %-127s |%n", "No audit logs found.");
        } else {
            for (ActivityLog l : logs) {
                System.out.printf(format, l.logId(), l.userId(), "AUDIT_EVENT", l.action(), l.logTime());
            }
        }
        System.out.println(border);
    }

    // === Input Validation Helpers ===

    private int readIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
            }
        }
    }

    private double readDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid amount. Please enter a valid numeric value (e.g. 150.00): ");
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