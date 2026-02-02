package ie.daoudz.model;

import java.time.LocalDateTime;

public record Customer(Long id, String fullName, String email, LocalDateTime createdAt) {

}
