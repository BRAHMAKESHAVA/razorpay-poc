package org.backend.repository;

import org.backend.model.Customer;
import org.backend.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsers(Users users);
        boolean existsByCustomerId(Long customerId);
}
