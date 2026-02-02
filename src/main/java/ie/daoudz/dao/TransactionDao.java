package ie.daoudz.dao;

import ie.daoudz.model.Transaction;
import ie.daoudz.model.TransactionType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionDao {

    private final DataSource dataSource;

    public TransactionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Transaction insert(Connection conn, Transaction draft) {
        String sql = """
                INSERT INTO transactions (account_id, tx_type, amount, reference, transfer_id)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, created_at
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, draft.accountId());
            ps.setString(2, draft.type().name());
            ps.setBigDecimal(3, draft.amount());
            ps.setString(4, draft.reference());
            ps.setObject(5, draft.transferId());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException("insert failed: no row returned.");
                }

                long id = rs.getLong("id");

                return new Transaction(
                        id,
                        draft.accountId(),
                        draft.type(),
                        draft.amount(),
                        draft.reference(),
                        draft.transferId(),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        }  catch (Exception e) {
            throw new RuntimeException("Failed to insert transaction for accountId: " + draft.accountId(), e);
        }
    }

    public List<Transaction> findByAccountId(long accountId) {
        String sql = """
                SELECT id, account_id, tx_type, amount, reference, transfer_id, created_at
                FROM transactions
                WHERE account_id = ?
                ORDER BY created_at DESC, id DESC
                """;

        List<Transaction> txList = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    txList.add(mapRowToTransaction(rs));
                }
            }

            return txList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load transactions fro account: " + accountId , e);
        }
    }

    public List<Transaction> findLastNByAccountId(long accountId, int n) {
        String sql = """
            SELECT id, account_id, tx_type, amount, reference, transfer_id, created_at
            FROM transactions
            WHERE account_id = ?
            ORDER BY created_at DESC, id DESC
            LIMIT ?
            """;

        List<Transaction> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, accountId);
            ps.setInt(2, n);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load last " + n + " transactions for accountId=" + accountId, e);
        }
    }


    public List<Transaction> findByAccountIdAndDateRange(long accountId,
                                                         LocalDateTime fromInclusive,
                                                         LocalDateTime toExclusive) {

        String sql = """
            SELECT id, account_id, tx_type, amount, reference, transfer_id, created_at
            FROM transactions
            WHERE account_id = ?
              AND created_at >= ?
              AND created_at < ?
            ORDER BY created_at DESC, id DESC
            """;

        List<Transaction> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, accountId);

            // JDBC expects java.sql.Timestamp for TIMESTAMP columns
            ps.setTimestamp(2, Timestamp.valueOf(fromInclusive));
            ps.setTimestamp(3, Timestamp.valueOf(toExclusive));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load transactions for accountId=" + accountId +
                            " from=" + fromInclusive + " to=" + toExclusive, e);
        }
    }


    private Transaction mapRowToTransaction(ResultSet rs) throws Exception {
        return new Transaction(
                rs.getLong("id"),
                rs.getLong("account_id"),
                TransactionType.valueOf(rs.getString("tx_type")),
                rs.getBigDecimal("amount"),
                rs.getString("reference"),
                rs.getObject("transfer_id", UUID.class),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

}
