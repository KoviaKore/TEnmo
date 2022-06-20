package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;



import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Objects;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService(API_BASE_URL);
    private final NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
    private AuthenticatedUser currentUser;
    private final TransferService transferService = new TransferService(API_BASE_URL);

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        BigDecimal balance;
        balance = accountService.balance(currentUser.getToken(), currentUser.getUser().getId());
        System.out.println("Your current account balance is: " + numberFormat.format(balance));
    }

    private void viewTransferHistory() {
        TransferHistory[] transfers = transferService.history(currentUser.getToken(), currentUser.getUser().getId());

    }

    private void viewPendingRequests() {
        // TODO Auto-generated method stub

    }

    private void sendBucks() {
        User[] users = accountService.getUsers(currentUser.getToken());
        System.out.println("-----------------------------------");
        System.out.println("Users");
        System.out.printf("%s %18s", "ID", "Name");
        System.out.println("\n-----------------------------------");
        for (User user : users) {
            if (user.getId().equals(currentUser.getUser().getId())) {
                continue;
            }
            long id = user.getId();
            String name = user.getUsername();
            System.out.printf("%s %18s \n", id, name);
        }
        System.out.println("\n---------");
        while (true) {
            for (User user : users) {
                long id = consoleService.promptForInt("Enter ID of user you are sending to (0 to cancel): ");
                //Back to Main Menu
                if (id == 0) {
                    break;
                }
                if (id != user.getId() || id == currentUser.getUser().getId()) {
                    System.out.println("Invalid ID");
                    continue;
                }

                BigDecimal amount = consoleService.promptForBigDecimal("Enter amount: ");
                while (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    amount = consoleService.promptForBigDecimal("Please enter a valid amount: ");
                }
                Transfer result = transferService.send(currentUser.getToken(), currentUser.getUser(), id, amount);
                System.out.println("Transfer successful! Transfer Id: " + result.getTransfer_id());
                mainMenu();
            }
        }

    }



    private void requestBucks() {
        // TODO Auto-generated method stub

    }

}


