package ie.daoudz.service;

import ie.daoudz.dao.AccountDao;
import ie.daoudz.dao.TransactionDao;
import ie.daoudz.model.Account;
import ie.daoudz.model.Transaction;
import ie.daoudz.model.TransactionType;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.UUID;

public class BankService {

    private final DataSource dataSource;
    private final AccountDao accountDao;
    private final TransactionDao transactionDao;

    public BankService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.accountDao = new AccountDao(dataSource);
        this.transactionDao = new TransactionDao(dataSource);
    }


    public void deposit(String accountNo, BigDecimal amount, String reference) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }

        try (var conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            try {
                Account account = accountDao.findByAccountNoForUpdate(conn, accountNo);

                if (account == null) {
                    throw new IllegalArgumentException("Account not found: " + accountNo);
                }

                BigDecimal newBalance = account.balance().add(amount);

                accountDao.updateBalance(conn, account.id(), newBalance);

                Transaction draftTx = new Transaction(
                        null,
                        account.id(),
                        TransactionType.DEPOSIT,
                        amount,
                        reference,
                        null,
                        null
                );

                transactionDao.insert(conn, draftTx);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Deposit failed for accountNo=" + accountNo, e);
        }
    }

    public void withdraw(String accountNo, BigDecimal amount, String reference) {

        if (amount == null) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }

        try (var conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Account account = accountDao.findByAccountNoForUpdate(conn, accountNo);

                if (account == null) {
                    throw new IllegalArgumentException("Account not found: " + accountNo);
                }

                if (account.balance().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient funds balance: " + account.balance());
                }

                BigDecimal newBalance = account.balance().subtract(amount);

                accountDao.updateBalance(conn, account.id() , newBalance);

                Transaction draftTx = new Transaction(
                        null,
                        account.id(),
                        TransactionType.WITHDRAW,
                        amount,
                        reference,
                        null,
                        null
                );
                transactionDao.insert(conn, draftTx);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Withdraw failed for accountNo=" + accountNo , e);
        }
    }

    public UUID transfer(String fromAccountNo, String toAccountNo, BigDecimal amount, String reference) {

        if (fromAccountNo == null || toAccountNo == null) {
            throw new IllegalArgumentException("Account numbers cannot be null");
        }

        if (fromAccountNo.equals(toAccountNo)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }

        UUID transferId = UUID.randomUUID();

        try (var conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                //lock both accounts -must be in a consistent order to avoid deadlock
                Account fromPreview = accountDao.findByAccountNo(fromAccountNo);
                Account toPreview = accountDao.findByAccountNo(toAccountNo);

                if (fromPreview == null) throw new IllegalArgumentException("sender account not found: " + fromAccountNo);
                if (toPreview == null) throw new IllegalArgumentException("receiver account not found: " + toAccountNo);

                long firstId = Math.min(fromPreview.id(), toPreview.id());
                long secondId = Math.max(fromPreview.id(), toPreview.id());

                var firstLocked = accountDao.findByIdForUpdate(conn, firstId);
                var secondLocked = accountDao.findByIdForUpdate(conn, secondId);

                var from = (fromPreview.id().equals(firstLocked.id())) ? firstLocked : secondLocked;
                var to = (toPreview.id().equals(firstLocked.id())) ? firstLocked : secondLocked;



                if (from.balance().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient funds. Balance: " + from.balance());
                }

                BigDecimal fromNewBalance = from.balance().subtract(amount);
                BigDecimal toNewBalance = to.balance().add(amount);

                accountDao.updateBalance(conn, from.id(), fromNewBalance);
                accountDao.updateBalance(conn, to.id(), toNewBalance);

                Transaction outTx = new Transaction(
                        null,
                        from.id(),
                        TransactionType.TRANSFER_OUT,
                        amount,
                        reference,
                        transferId,
                        null
                );

                Transaction inTx = new Transaction(
                        null,
                        to.id(),
                        TransactionType.TRANSFER_IN,
                        amount,
                        reference,
                        transferId,
                        null
                );

                transactionDao.insert(conn, outTx);
                transactionDao.insert(conn, inTx);

                conn.commit();

                return transferId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("Transfer failed transferId=" + transferId , e);
        }
    }
}
