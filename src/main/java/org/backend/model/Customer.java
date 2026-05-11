package org.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a customer in the system.
 * Each customer is associated with a user account.
 */
@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users users;

    @CreationTimestamp
    private LocalDateTime createdDate;
}
