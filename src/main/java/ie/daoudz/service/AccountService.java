package ie.daoudz.service;

import ie.daoudz.dao.AccountDao;
import ie.daoudz.model.Account;
import ie.daoudz.model.AccountStatus;
import ie.daoudz.model.AccountType;

import java.math.BigDecimal;
import java.util.List;

public class AccountService {

    private final AccountDao accountDao;

    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public Account openAccount(long customerId, AccountType type) {

        // 1) Validate inputs (business rules)
        if (customerId <= 0) {
            throw new IllegalArgumentException("Customer id must be > 0");
        }

        if (type == null) {
            throw new IllegalArgumentException("Account type is required.");
        }

        if (accountDao.existsByCustomerIdAndType(customerId, type)) {
            throw new IllegalArgumentException("Customer already has a " + type + " account" );
        }


        String accountNo = generateAccountNo();

        // 2) Draft account (id and createdAt unknown until DB insert)
        Account draft = new Account(
                null,
                customerId,
                accountNo,
                type,
                new BigDecimal("0.00"),
                AccountStatus.ACTIVE,
                null
        );

        // 3) DAO insert (JDBC)
        return accountDao.openAccount(draft);
    }

    public List<Account> listAccountsByCustomerId(long customerId) {
        if (customerId <= 0) throw new IllegalArgumentException("Customer id must be > 0");
        return accountDao.findByCustomerId(customerId);
    }

    public Account findByAccountNo(String accountNo) {
        if (accountNo == null || accountNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required.");
        }
        return accountDao.findByAccountNo(accountNo.trim().toUpperCase());
    }



    public String generateAccountNo() {
        int random = (int) (Math.random() * 900) + 100;
        return "AC" + System.currentTimeMillis() + random;
    }
}

