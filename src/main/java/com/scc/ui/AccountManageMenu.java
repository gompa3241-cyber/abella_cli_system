package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.Userdao;
import com.scc.model.ActivityLog;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {

    private final Userdao userdao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public AccountManageMenu(Userdao userdao, Scanner scanner) {
        this.userdao = userdao;
        this.logDao = new ActivityLogDao();
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
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser(admin);
                case "4" -> addNewUser(admin);
                case "5" -> updateRoleAndStatus(admin);
                case "6" -> deleteUser(admin);
                case "7" -> auditLogsMenu();
                case "8" -> {
                    logDao.logAction(admin.user_id(), "USER_LOGOUT", "Admin " + admin.username() + " (ID: " + admin.user_id() + ") logged out.");
                    System.out.println("Logging out from Admin Dashboard...");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] Invalid Option. Please choose between 1 and 8.");
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

    private void approveUser(user admin) {
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
            logDao.logAction(admin.user_id(), "USER_APPROVED", "Admin " + admin.user_id() + " approved account for User ID " + userId);
            System.out.println("-> User ID " + userId + " approved successfully!");
        } else {
            System.out.println("-> Failed to approve user.");
        }
    }

    private void addNewUser(user admin) {
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
            logDao.logAction(admin.user_id(), "USER_CREATED", "Admin " + admin.user_id() + " added new user: " + username);
            System.out.println("-> New user added successfully!");
        } else {
            System.out.println("-> Failed to add user.");
        }
    }

    private void updateRoleAndStatus(user currentAdmin) {
        displayUserTable(userdao.getAllUser(), "SELECT USER TO UPDATE");

        int userId = readIntInput("Enter User ID to update (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println("-> Operation cancelled.");
            return;
        }

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
            logDao.logAction(currentAdmin.user_id(), "USER_UPDATED", "Admin " + currentAdmin.user_id() + " changed role of User " + userId + " to " + newRole);
            System.out.println("-> User updated successfully!");
        } else {
            System.out.println("-> Failed to update user.");
        }
    }

    private void deleteUser(user admin) {
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
                logDao.logAction(admin.user_id(), "USER_DELETED", "Admin " + admin.user_id() + " deleted user ID " + userId);
                System.out.println("-> User deleted successfully!");
            } else {
                System.out.println("-> Failed to delete user.");
            }
        } else {
            System.out.println("-> Deletion cancelled.");
        }
    }

    private void auditLogsMenu() {
        boolean inAuditMenu = true;
        while (inAuditMenu) {
            System.out.println("\n=================================");
            System.out.println("       SYSTEM AUDIT LOGS         ");
            System.out.println("=================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Back to Admin Dashboard");
            System.out.print("Choose an option (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> displayLogs(logDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
                case "2" -> {
                    int userId = readIntInput("Enter User ID to filter: ");
                    displayLogs(logDao.getLogsByUserId(userId), "LOGS FOR USER ID: " + userId);
                }
                case "3" -> {
                    System.out.print("Are you sure you want to clear all audit logs? (y/n): ");
                    String confirm = scanner.nextLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        if (logDao.clearLogs()) {
                            System.out.println("-> All activity logs cleared successfully.");
                        } else {
                            System.out.println("-> Failed to clear logs.");
                        }
                    } else {
                        System.out.println("-> Operation cancelled.");
                    }
                }
                case "4" -> inAuditMenu = false;
                default -> System.out.println("-> [ERROR] Invalid Option. Please choose between 1 and 4.");
            }
        }
    }

    private void displayLogs(List<ActivityLog> logs, String title) {
        System.out.println("\n==================================================================================================");
        System.out.println("                                " + title);
        System.out.println("==================================================================================================");
        if (logs.isEmpty()) {
            System.out.println("-> No Activity Logs Found.");
            return;
        }
        System.out.printf("%-5s | %-8s | %-18s | %-35s | %-20s%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (ActivityLog log : logs) {
            System.out.printf("%-5s | %-8s | %-18s | %-35s | %-20s%n",
                    log.id(), log.userId(), log.actionType(), log.description(), log.timestamp());
        }
        System.out.println("==================================================================================================");
    }

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