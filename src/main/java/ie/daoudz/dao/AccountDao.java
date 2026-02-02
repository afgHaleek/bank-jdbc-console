package ie.daoudz.dao;

import ie.daoudz.model.Account;
import ie.daoudz.model.AccountStatus;
import ie.daoudz.model.AccountType;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {

    private final DataSource dataSource;

    public AccountDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public Account openAccount(Account draft) {
        String sql = """
                INSERT INTO accounts (customer_id, account_no, type, balance, status)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, created_at
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, draft.customerId());
            ps.setString(2, draft.accountNo());
            ps.setString(3, draft.type().name());
            ps.setBigDecimal(4, draft.balance());
            ps.setString(5, draft.status().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Failed to open account for customer: " + draft.customerId());
                }

                long id = rs.getLong("id");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                return new Account(
                        id,
                        draft.customerId(),
                        draft.accountNo(),
                        draft.type(),
                        draft.balance(),
                        draft.status(),
                        createdAt
                );
            }
        }catch (Exception e) {
            throw new RuntimeException("Failed to open account for customer =" + draft.customerId(),e);
        }
    }

    public Account findByAccountNo(String accountNo) {
        String sql = """
                SELECT id, customer_id, account_no, type, balance, status, created_at
                FROM accounts
                WHERE account_no = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAccount(rs);
                }

                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find account by accountNo: " + accountNo, e);
        }
    }

    public boolean existsByCustomerIdAndType(long customerId, AccountType type) {
        String sql = """
                SELECT 1
                FROM accounts
                WHERE customer_id = ?
                 AND type = ?
                LIMIT 1
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);
            ps.setString(2, type.name());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check account existence by customerId and type", e);
        }
    }

    public List<Account> findByCustomerId(long customerId) {
        String sql = """
            SELECT id, customer_id, account_no, type, balance, status, created_at
            FROM accounts
            WHERE customer_id = ?
            ORDER BY created_at DESC, id DESC
            """;

        List<Account> accounts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRowToAccount(rs));
                }
            }

            return accounts;

        } catch (Exception e) {
            throw new RuntimeException("Failed to list accounts for customerId=" + customerId, e);
        }
    }


    public Account findByAccountNoForUpdate(Connection conn, String accountNo) {
        String sql = """
            SELECT id, customer_id, account_no, type, balance, status, created_at
            FROM accounts
            WHERE account_no = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAccount(rs);
                }
                return null;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to lock and load accountNo=" + accountNo, e);
        }
    }


    public Account findByIdForUpdate(Connection conn, long accountId) {
        String sql = """
                SELECT id, customer_id, account_no, type, balance, status, created_at
                FROM accounts
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAccount(rs);
                }

                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to lock and load accountId: " + accountId,e);
        }
    }

    public void updateBalance(Connection conn, long accountId, BigDecimal newBalance) {
        String sql = """
                UPDATE accounts
                SET balance = ?
                WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, accountId);

            int rows = ps.executeUpdate();

            if (rows != 1) {
                throw new RuntimeException("Failed to update balance for account ID: " + accountId);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to update balance for accountId: " + accountId, e);
        }
    }

    private Account mapRowToAccount(ResultSet rs) throws Exception {
        return new Account(
                rs.getLong("id"),
                rs.getLong("customer_id"),
                rs.getString("account_no"),
                AccountType.valueOf(rs.getString("type")),
                rs.getBigDecimal("balance"),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
