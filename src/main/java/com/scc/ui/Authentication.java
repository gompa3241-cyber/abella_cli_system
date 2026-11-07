package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.Userdao;
import com.scc.model.user;

import java.util.Scanner;

public class Authentication {
    private final Userdao userDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public Authentication(Userdao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n================================");
            System.out.println("           COINWARD              ");
            System.out.println("================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    System.out.println("Exiting application. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option selected!");
            }
        }
    }

    private void handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        user loggedInUser = userDao.login(username, password);

        if (loggedInUser != null) {
            logDao.logAction(loggedInUser.user_id(), "USER_LOGIN", "User " + loggedInUser.username() + " (ID: " + loggedInUser.user_id() + ") logged in.");
            System.out.println("\n -> Welcome " + loggedInUser.username() + " | roles: " + loggedInUser.roles());

            switch (loggedInUser.roles().toUpperCase()) {
                case "ADMIN", "ADMINISTRATOR" -> new AccountManageMenu(userDao, scanner).display(loggedInUser);
                default -> System.out.println("Account identity menu not found for role: " + loggedInUser.roles());
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void handleRegister() {
        System.out.print("Enter new username: ");
        String users = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        System.out.print("Select a role [1] ADMIN [2] STAFF [3] CUSTOMER: ");
        int rolesChoice;
        try {
            rolesChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid role choice!");
            return;
        }
        String roles;

        switch (rolesChoice) {
            case 1:
                roles = "ADMIN";
                break;
            case 2:
                roles = "STAFF";
                break;
            case 3:
                roles = "CUSTOMER";
                break;
            default:
                roles = null;
        }

        if (roles != null) {
            userDao.registerUser(users, password, roles);
            System.out.println("Registration successful!");
        } else {
            System.out.println("Invalid role selection!");
        }
    }
}