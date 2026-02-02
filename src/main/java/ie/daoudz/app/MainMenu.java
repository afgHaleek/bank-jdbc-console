package ie.daoudz.app;


import ie.daoudz.dao.AccountDao;
import ie.daoudz.dao.CustomerDao;
import ie.daoudz.dao.TransactionDao;
import ie.daoudz.service.AccountService;
import ie.daoudz.service.BankService;
import ie.daoudz.service.CustomerService;
import ie.daoudz.service.StatementService;
import ie.daoudz.util.Input;

import javax.sql.DataSource;

public class MainMenu {

    private final Input input = new Input();
    private final CustomerMenu customerMenu;
    private final AccountsMenu accountsMenu;
    private final TransactionMenu transactionMenu;

    public MainMenu(DataSource dataSource) {
        CustomerDao customerDao = new CustomerDao(dataSource);
        CustomerService customerService = new CustomerService(customerDao);
        AccountDao accountDao = new AccountDao(dataSource);
        AccountService accountService = new AccountService(accountDao);
        BankService bankService = new BankService(dataSource);
        TransactionDao transactionDao = new TransactionDao(dataSource);
        StatementService statementService = new StatementService(transactionDao);
        var selector = new Selector(input, customerService, accountService);

        this.accountsMenu = new AccountsMenu(input, accountService, customerService, selector);
        this.customerMenu = new CustomerMenu(input, customerService);
        this.transactionMenu = new TransactionMenu(input, bankService, selector, accountService, statementService);
    }

    public void start() {
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1) Customers");
            System.out.println("2) Accounts");
            System.out.println("3) Transactions");
            System.out.println("0) Exit");

            int choice = input.readInt("Choose: ");

            switch (choice) {
                case 1 -> customerMenu.start();
                case 2 -> accountsMenu.start();
                case 3 -> transactionMenu.start();
                case 0 -> {
                    System.out.println("Bye.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
