package ie.daoudz.service;


import ie.daoudz.dao.CustomerDao;
import ie.daoudz.model.Customer;

import java.util.List;

public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public Customer createCustomer(String fullName, String email) {

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email format looks invalid.");
        }

        Customer draft = new Customer(null, fullName.trim(), email.trim().toLowerCase(), null);

        return customerDao.create(draft);
    }

    public List<Customer> listCustomers(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException("Page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }

        return customerDao.findPage(page, size);
    }

}

