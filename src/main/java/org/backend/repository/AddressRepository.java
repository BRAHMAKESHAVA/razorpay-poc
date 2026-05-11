package org.backend.repository;

import org.backend.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Address entities.
 * Provides CRUD operations and custom queries for customer addresses.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Finds an address by its ID and the associated customer ID.
     *
     * @param addressId the ID of the address
     * @param customerId the ID of the customer
     * @return an Optional containing the address if found
     */
    Optional<Address> findByAddressIdAndCustomerId(Long addressId, Long customerId);

    /**
     * Finds all addresses for a specific customer.
     *
     * @param customerId the ID of the customer
     * @return a list of addresses for the customer
     */
    List<Address> findByCustomerId(Long customerId);

    /**
     * Resets the default flag for all addresses of a customer.
     *
     * @param customerId the ID of the customer
     */
    @Modifying @Query("UPDATE Address a SET a.isDefault = false WHERE a.customerId = :customerId AND a.isDefault = true")
    void resetDefaultForCustomer(Long customerId);

    /**
     * Resets the default flag for all addresses of a customer except the specified address.
     *
     * @param customerId the ID of the customer
     * @param addressId the ID of the address to exclude
     */
    @Modifying @Query("UPDATE Address a SET a.isDefault = false WHERE a.customerId = :customerId AND a.addressId <> :addressId")
    void resetDefaultForCustomer(Long customerId, Long addressId);

    /**
     * Counts the number of addresses for a specific customer.
     *
     * @param customerId the ID of the customer
     * @return the count of addresses
     */
    long countByCustomerId(Long customerId);
}