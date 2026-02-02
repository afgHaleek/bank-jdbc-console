package ie.daoudz.app;

import ie.daoudz.model.Account;
import ie.daoudz.model.AccountType;
import ie.daoudz.service.AccountService;
import ie.daoudz.service.CustomerService;
import ie.daoudz.util.Input;

public class AccountsMenu {

    private final Input input;
    private final AccountService accountService;
    private final CustomerService customerService;
    private final Selector selector;

    public AccountsMenu(Input input, AccountService accountService, CustomerService customerService, Selector selector) {
        this.input = input;
        this.accountService = accountService;
        this.customerService = customerService;
        this.selector = selector;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== ACCOUNTS MENU ===");
            System.out.println("1) Open account");
            System.out.println("0) Back");

            int choice = input.readInt("Choose: ");

            switch (choice) {
                case 1 -> openAccount();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void openAccount() {
        try {

            var customer = selector.selectCustomerPaged();

            if (customer == null) return;

            // 4) Account type selection by number
            System.out.println("\nSelect account type:");
            System.out.println("1) CHECKING");
            System.out.println("2) SAVINGS");

            int typeChoice = input.readInt("Choose (1-2): ");
            var type = switch (typeChoice) {
                case 1 -> AccountType.CHECKING;
                case 2 -> AccountType.SAVINGS;
                default -> throw new IllegalArgumentException("Invalid account type choice.");
            };

            // 5) Service opens account (service generates account number)
            Account saved = accountService.openAccount(customer.id(), type);

            System.out.println("✅ Account opened. Account No: " + saved.accountNo());
            System.out.println(saved);

        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }

}

