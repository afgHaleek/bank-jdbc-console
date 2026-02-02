package ie.daoudz.service;

import ie.daoudz.dao.TransactionDao;
import ie.daoudz.model.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StatementService {

    private final TransactionDao transactionDao;

    public StatementService(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public List<Transaction> getLastN(long accountId, int n) {
        if (accountId <= 0) throw new IllegalArgumentException("Account id must be > 0");
        if (n <= 0 || n > 200) throw new IllegalArgumentException("N must be between 1 and 200");
        return transactionDao.findLastNByAccountId(accountId, n);
    }


    public List<Transaction> getByDateRange(long accountId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {

        if (accountId <= 0) throw new IllegalArgumentException("Account id must be > 0");
        if (fromDateInclusive == null || toDateInclusive == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        if (fromDateInclusive.isAfter(toDateInclusive)) {
            throw new IllegalArgumentException("'From' date must be <= 'To' date");
        }

        // Convert dates to timestamps:
        // from = start of day
        LocalDateTime from = fromDateInclusive.atStartOfDay();

        // toExclusive = start of next day (so we include the full 'to' date)
        LocalDateTime toExclusive = toDateInclusive.plusDays(1).atStartOfDay();

        return transactionDao.findByAccountIdAndDateRange(accountId, from, toExclusive);
    }

}
