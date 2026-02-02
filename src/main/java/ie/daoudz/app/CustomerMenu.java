package ie.daoudz.app;


import ie.daoudz.model.Customer;
import ie.daoudz.service.CustomerService;
import ie.daoudz.util.Input;

public class CustomerMenu {

    private final Input input;
    private final CustomerService customerService;

    public CustomerMenu(Input input, CustomerService customerService) {
        this.input = input;
        this.customerService = customerService;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== CUSTOMERS MENU ===");
            System.out.println("1) Create customer");
            System.out.println("2) List customers (paged)");
            System.out.println("0) Back");

            int choice = input.readInt("Choose: ");

            switch (choice) {
                case 1 -> createCustomer();
                case 2 -> listCustomersPaged();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void createCustomer() {
        try {
            String name = input.readString("Full name: ");
            String email = input.readString("Email: ");

            Customer saved = customerService.createCustomer(name, email);

            System.out.println("✅ Customer created:");
            System.out.println(saved);

        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }

    private void listCustomersPaged() {
        try {
            int size = input.readInt("Page size (e.g., 5 or 10): ");
            int pageNumberForUser = input.readInt("Page number (1,2,3...): ");

            int page = pageNumberForUser - 1;

            var customers = customerService.listCustomers(page, size);

            if (customers.isEmpty()) {
                System.out.println("No customers found for this page.");
                return;
            }

            System.out.println("\n--- Customers (page " + pageNumberForUser + ", size " + size + ") ---");
            customers.forEach(System.out::println);

        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }

}

