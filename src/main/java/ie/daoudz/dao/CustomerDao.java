package ie.daoudz.dao;

import ie.daoudz.model.Customer;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    private final DataSource dataSource;

    public CustomerDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Customer create(Customer customer) {
        String sql = """
                INSERT INTO customers (full_name, email)
                VALUES (?, ?)
                RETURNING id, created_at
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.fullName());
            ps.setString(2, customer.email());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Insert failed: no row returned");
                }

                long id = rs.getLong("id");
                Timestamp createdAts = rs.getTimestamp("created_at");

                return new Customer(
                        id,
                        customer.fullName(),
                        customer.email(),
                        createdAts.toLocalDateTime()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create customer", e);
        }
    }

    public List<Customer> findAll() {
        String sql = """
                SELECT id, full_name, email, created_at
                FROM customers
                ORDER BY id
                """;
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRowToCustomer(rs));
            }

            return customers;

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customers", e);
        }
    }

    public List<Customer> findPage(int page, int size) {
        String sql = """
                SELECT id, full_name, email, created_at
                FROM customers
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        List<Customer> customers = new ArrayList<>();
        int offset = page * size;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    customers.add(mapRowToCustomer(rs));
                }
            }

            return customers;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find customer page: page=" + page + ", size=" + size,e);
        }
    }

    public Customer findById(long id) {
        String sql = """
                SELECT id, full_name, email, created_at
                FROM customers
                WHERE id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }

                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to find customer by id: " + id);
        }
    }

    public Customer findByEmail(String email) {
        String sql = """
                SELECT id, full_name, email, created_at
                FROM customers
                WHERE email = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRowToCustomer(rs);
                }

                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("can not find customer with email: " + email);
        }
    }

    private Customer mapRowToCustomer(ResultSet rs) throws Exception {
        return new Customer(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
