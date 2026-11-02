package com.scc.ui;

import com.scc.dao.Userdao;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {

    private final Userdao userdao;
    private final Scanner scanner;

    public AccountManageMenu(Userdao userdao, Scanner scanner) {
        this.userdao = userdao;
        this.scanner = scanner;
    }

    public void display(user admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================");
            System.out.println("          ADMIN DASHBOARD        ");
            System.out.println("=================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> addNewUser();
                case "5" -> updateRoleAndStatus(admin); // Updated to pass current active admin session
                case "6" -> deleteUser();
                case "7" -> {
                    System.out.println("Logging out from Admin Dashboard...");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option. Please choose between 1 and 7.");
            }
        }
    }

    private void displayUserTable(List<user> users, String title) {
        System.out.println("\n=========================" + title + "=========================");
        if (users.isEmpty()) {
            System.out.println("-> No Records Found");
            return;
        }
        System.out.println("+----+-----------------+------------+------------+");
        System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+----+-----------------+------------+------------+");
        for (user u : users) {
            System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n",
                    u.user_id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+----+-----------------+------------+------------+");
    }

    private void viewAllUser() {
        displayUserTable(userdao.getAllUser(), "ALL REGISTERED USERS");
    }

    private void viewPendingUsers() {
        displayUserTable(userdao.getPendingUsers(), "PENDING USER APPROVALS");
    }

    private void approveUser() {
        List<user> pending = userdao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("-> No pending users to approve.");
            return;
        }
        displayUserTable(pending, "PENDING USERS");
        int userId = readIntInput("Enter User ID to approve (or 0 to cancel): ");

        if (userId == 0) {
            System.out.println("-> Operation cancelled.");
            return;
        }

        boolean exists = pending.stream().anyMatch(u -> u.user_id() == userId);
        if (!exists) {
            System.out.println("-> User ID not found in pending list.");
            return;
        }

        if (userdao.updateStatus(userId, "APPROVED")) {
            System.out.println("-> User ID " + userId + " approved successfully!");
        } else {
            System.out.println("-> Failed to approve user.");
        }
    }

    private void addNewUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        String role = readValidRole();
        if (role == null) {
            System.out.println("-> Operation cancelled.");
            return;
        }

        String status = readValidStatus();
        if (status == null) {
            System.out.println("-> Operation cancelled.");
            return;
        }

        if (userdao.addUserByAdmin(username, password, role, status)) {
            System.out.println("-> New user added successfully!");
        } else {
            System.out.println("-> Failed to add user.");
        }
    }

    // New update process following tutorial logic with strict validation & active admin check
    private void updateRoleAndStatus(user currentAdmin) {
        displayUserTable(userdao.getAllUser(), "SELECT USER TO UPDATE");

        int userId = readIntInput("Enter User ID to update (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println("-> Operation cancelled.");
            return;
        }

        // Prevent admin from modifying their own active account session
        if (userId == currentAdmin.user_id()) {
            System.out.println("-> [WARNING] Modifying current active admin session is restricted!");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("-> Operation cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("-> Operation cancelled.");
            return;
        }

        if (userdao.updateUser(userId, newRole, newStatus)) {
            System.out.println("-> User updated successfully!");
        } else {
            System.out.println("-> Failed to update user.");
        }
    }

    private void deleteUser() {
        viewAllUser();
        int userId = readIntInput("Enter User ID to delete (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println("-> Operation cancelled.");
            return;
        }

        System.out.print("Are you sure you want to delete user ID " + userId + "? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y")) {
            if (userdao.deleteUser(userId)) {
                System.out.println("-> User deleted successfully!");
            } else {
                System.out.println("-> Failed to delete user.");
            }
        } else {
            System.out.println("-> Deletion cancelled.");
        }
    }

    // Helper: Validates numeric ID entries safely
    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("-> [ERROR] Invalid numeric format. Please enter a valid number.");
            }
        }
    }

    // Helper: Validates proper role entries
    private String readValidRole() {
        while (true) {
            System.out.print("Enter role [ADMIN / STAFF / CUSTOMER] (or 'cancel'): ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0") || role.equalsIgnoreCase("CANCEL")) {
                return null;
            }
            if (role.equals("ADMIN") || role.equals("STAFF") || role.equals("CUSTOMER")) {
                return role;
            }
            System.out.println("-> [ERROR] Invalid role. Please choose between ADMIN, STAFF, or CUSTOMER.");
        }
    }

    // Helper: Validates proper status entries
    private String readValidStatus() {
        while (true) {
            System.out.print("Enter status [APPROVED / PENDING] (or 'cancel'): ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0") || status.equalsIgnoreCase("CANCEL")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("-> [ERROR] Invalid status. Please enter APPROVED or PENDING.");
        }
    }
}