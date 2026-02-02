package ie.daoudz.app;

import ie.daoudz.model.Account;
import ie.daoudz.model.Customer;
import ie.daoudz.service.AccountService;
import ie.daoudz.service.CustomerService;
import ie.daoudz.util.Input;

import java.util.List;

public class Selector {


    private final Input input;
    private final CustomerService customerService;
    private final AccountService accountService;

    public Selector(Input input, CustomerService customerService, AccountService accountService) {
        this.input = input;
        this.customerService = customerService;
        this.accountService = accountService;
    }

    public Customer selectCustomerPaged() {
        int size = input.readInt("Customers per page (e.g., 5 or 10): ");
        int pageNumberForUser = input.readInt("Customer page number (1,2,3...): ");
        int page = pageNumberForUser - 1;

        List<Customer> customers = customerService.listCustomers(page, size);

        if (customers.isEmpty()) {
            System.out.println("No customers found on this page.");
            return null;
        }

        System.out.println("\n--- Customers (page " + pageNumberForUser + ") ---");
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            System.out.println((i + 1) + ") " + c.fullName() + " | " + c.email() + " | id=" + c.id());
        }

        int pick = input.readInt("Select customer (1-" + customers.size() + "): ");
        if (pick < 1 || pick > customers.size()) {
            System.out.println("Invalid selection.");
            return null;
        }

        return customers.get(pick - 1);
    }

    public Account selectAccountForCustomer(long customerId) {
        List<Account> accounts = accountService.listAccountsByCustomerId(customerId);

        if (accounts.isEmpty()) {
            System.out.println("This customer has no accounts.");
            return null;
        }

        System.out.println("\n--- Accounts ---");
        for (int i = 0; i < accounts.size(); i++) {
            Account a = accounts.get(i);
            System.out.println((i + 1) + ") " + a.accountNo() +
                    " | " + a.type() +
                    " | balance=" + a.balance() +
                    " | " + a.status());
        }

        int pick = input.readInt("Select account (1-" + accounts.size() + "): ");
        if (pick < 1 || pick > accounts.size()) {
            System.out.println("Invalid selection.");
            return null;
        }

        return accounts.get(pick - 1);
    }
}
