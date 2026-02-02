package ie.daoudz.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Account(
        Long id,
        Long customerId,
        String accountNo,
        AccountType type,
        BigDecimal balance,
        AccountStatus status,
        LocalDateTime createdAt
) {
}
