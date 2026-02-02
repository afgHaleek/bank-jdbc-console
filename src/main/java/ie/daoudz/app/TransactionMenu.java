package ie.daoudz.app;

import ie.daoudz.service.AccountService;
import ie.daoudz.service.BankService;
import ie.daoudz.service.StatementService;
import ie.daoudz.util.Input;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionMenu {

    private final Input input;
    private final BankService bankService;
    private final AccountService accountService;
    private final StatementService statementService;
    private final Selector selector;

    public TransactionMenu(Input input, BankService bankService, Selector selector, AccountService accountService, StatementService statementService) {
        this.input = input;
        this.bankService = bankService;
        this.accountService = accountService;
        this.statementService = statementService;
        this.selector = selector;
    }

    public void start() {
        while (true) {
            System.out.println("\n=== TRANSACTIONS MENU ===");
            System.out.println("1) Deposit");
            System.out.println("2) Withdraw");
            System.out.println("3) Transfer");
            System.out.println("4) Check balance");
            System.out.println("5) Statement (last N)");
            System.out.println("6) Statement (date range)");
            System.out.println("0) Back");

            int choice = input.readInt("Choose: ");

            switch (choice) {
                case 1 -> deposit();
                case 2 -> withdraw();
                case 3 -> transfer();
                case 4 -> checkBalance();
                case 5 -> statementLastN();
                case 6 -> statementDateRange();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void deposit() {
        try {

            var customer = selector.selectCustomerPaged();

            if (customer == null) return;

            var account = selector.selectAccountForCustomer(customer.id());

            if (account == null) return;

            // Read amount as string then convert to BigDecimal (safe for money)
            String amountStr = input.readString("Amount (e.g., 50.00): ");
            BigDecimal amount = new BigDecimal(amountStr);

            String ref = input.readString("Reference (optional): ");
            if (ref.isBlank()) ref = null;

            bankService.deposit(account.accountNo(), amount, ref);

            System.out.println("✅ Deposit successful.");

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount format. Example: 50.00");
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }

    private void withdraw() {
        try {
            var customer = selector.selectCustomerPaged();
            if (customer == null) return;

            var account = selector.selectAccountForCustomer(customer.id());
            if (account == null) return;

            String amountStr = input.readString("Amount (e.g., 50.00): ");
            BigDecimal amount = new BigDecimal(amountStr);

            String ref = input.readString("Reference (optional): ");
            if (ref.isBlank()) ref = null;

            bankService.withdraw(account.accountNo(), amount, ref);

            System.out.println("✅ Withdraw successful from " + account.accountNo());

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount format. Example: 50.00");
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }

    private void checkBalance() {
        try {
            var customer = selector.selectCustomerPaged();
            if (customer == null) return;

            var account = selector.selectAccountForCustomer(customer.id());
            if (account == null) return;

            // fetch latest from DB (balance might have changed)
            var fresh = accountService.findByAccountNo(account.accountNo());

            if (fresh == null) {
                System.out.println("Account not found anymore.");
                return;
            }

            System.out.println("✅ Account: " + fresh.accountNo());
            System.out.println("Type: " + fresh.type());
            System.out.println("Status: " + fresh.status());
            System.out.println("Balance: " + fresh.balance());

        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }

    private void transfer() {
        try {
            System.out.println("\n--- Select SENDER ---");
            var senderCustomer = selector.selectCustomerPaged();
            if (senderCustomer == null) return;

            var senderAccount = selector.selectAccountForCustomer(senderCustomer.id());
            if (senderAccount == null) return;

            System.out.println("\n--- Select RECEIVER ---");
            var receiverCustomer = selector.selectCustomerPaged();
            if (receiverCustomer == null) return;

            var receiverAccount = selector.selectAccountForCustomer(receiverCustomer.id());
            if (receiverAccount == null) return;

            if (senderAccount.accountNo().equals(receiverAccount.accountNo())) {
                System.out.println("❌ Sender and receiver accounts cannot be the same.");
                return;
            }

            String amountStr = input.readString("Amount (e.g., 50.00): ");
            BigDecimal amount = new BigDecimal(amountStr);

            String ref = input.readString("Reference (optional): ");
            if (ref.isBlank()) ref = null;

            var transferId = bankService.transfer(
                    senderAccount.accountNo(),
                    receiverAccount.accountNo(),
                    amount,
                    ref
            );

            System.out.println("✅ Transfer successful.");
            System.out.println("Transfer ID: " + transferId);

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid amount format. Example: 50.00");
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }

    private void statementLastN() {
        try {
            var customer = selector.selectCustomerPaged();
            if (customer == null) return;

            var account = selector.selectAccountForCustomer(customer.id());
            if (account == null) return;

            int n = input.readInt("How many recent transactions? (e.g., 10): ");

            var txs = statementService.getLastN(account.id(), n);

            System.out.println("\n=== STATEMENT: " + account.accountNo() + " (last " + n + ") ===");

            if (txs.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }

            for (var tx : txs) {
                System.out.println(tx.createdAt() + " | " +
                        tx.type() + " | " +
                        tx.amount() +
                        (tx.reference() != null ? " | " + tx.reference() : "") +
                        (tx.transferId() != null ? " | transferId=" + tx.transferId() : "")
                );
            }

        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }


    private void statementDateRange() {
        try {
            var customer = selector.selectCustomerPaged();
            if (customer == null) return;

            var account = selector.selectAccountForCustomer(customer.id());
            if (account == null) return;

            // Read dates in ISO format: yyyy-MM-dd
            String fromStr = input.readString("From date (yyyy-MM-dd): ");
            String toStr = input.readString("To date (yyyy-MM-dd): ");

            LocalDate from = LocalDate.parse(fromStr);
            LocalDate to = LocalDate.parse(toStr);

            var txs = statementService.getByDateRange(account.id(), from, to);

            System.out.println("\n=== STATEMENT: " + account.accountNo() +
                    " (" + from + " to " + to + ") ===");

            if (txs.isEmpty()) {
                System.out.println("No transactions found in this range.");
                return;
            }

            for (var tx : txs) {
                System.out.println(tx.createdAt() + " | " +
                        tx.type() + " | " +
                        tx.amount() +
                        (tx.reference() != null ? " | " + tx.reference() : "") +
                        (tx.transferId() != null ? " | transferId=" + tx.transferId() : "")
                );
            }

        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use yyyy-MM-dd (example: 2026-01-26)");
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
    }





}
