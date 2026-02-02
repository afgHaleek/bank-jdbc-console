package ie.daoudz.model;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
        Long id,
        Long accountId,
        TransactionType type,
        BigDecimal amount,
        String reference,
        UUID transferId,
        LocalDateTime createdAt
) { }
