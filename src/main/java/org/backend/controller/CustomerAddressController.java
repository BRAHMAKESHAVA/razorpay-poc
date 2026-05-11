package org.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.backend.dto.common.AddressDTO;
import org.backend.dto.common.ApiResponse;
import org.backend.model.Address;
import org.backend.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing customer addresses.
 * This controller provides endpoints for creating, updating, deleting, and retrieving
 * customer addresses. All endpoints require a valid customer ID and operate under
 * the base path "/api/customer-address".
 */
@RestController
@RequestMapping("/api/customer-address")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final AddressService addressService;

    /**
     * Creates a new address for the specified customer.
     *
     * @param customerId the ID of the customer for whom the address is being created
     * @param address the address details to be created
     * @return ResponseEntity containing the API response with the created address data
     */
    @PostMapping("/{customerId}")
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(@PathVariable Long customerId, @Valid @RequestBody Address address) {
        return ResponseEntity.ok()
                .body(ApiResponse.<AddressDTO>builder()
                        .status(true)
                        .message("Address created successfully")
                        .data(addressService.createAddress(customerId, address))
                        .build());
    }

    /**
     * Updates an existing address for the specified customer.
     *
     * @param customerId the ID of the customer whose address is being updated
     * @param addressId the ID of the address to be updated
     * @param addressDTO the updated address details
     * @return ResponseEntity containing the API response with the updated address data
     */
    @PutMapping("/{customerId}/update/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(@PathVariable Long customerId, @PathVariable Long addressId, @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(
                ApiResponse.<AddressDTO>builder()
                        .status(true)
                        .message("Address updated successfully")
                        .data(addressService.updateAddress(customerId, addressId, addressDTO))
                        .build()
        );
    }

    /**
     * Deletes an address for the specified customer.
     *
     * @param customerId the ID of the customer whose address is being deleted
     * @param addressId the ID of the address to be deleted
     * @return ResponseEntity containing the API response confirming the deletion
     */
    @DeleteMapping("/{customerId}/delete/{addressId}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(@PathVariable Long customerId, @PathVariable Long addressId) {
        addressService.deleteAddress(customerId, addressId);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(true)
                        .message("Address deleted successfully")
                        .data(null)
                        .build()

        );
    }

    /**
     * Retrieves a specific address by ID for the specified customer.
     *
     * @param customerId the ID of the customer whose address is being retrieved
     * @param addressId the ID of the address to be retrieved
     * @return ResponseEntity containing the API response with the address data
     */
    @GetMapping("/{customerId}/address/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> getAddressById(@PathVariable Long customerId, @PathVariable Long addressId) {
        return ResponseEntity.ok(
                ApiResponse.<AddressDTO>builder()
                        .status(true)
                        .message("Address fetched successfully")
                        .data(addressService.getAddressById(customerId, addressId))
                        .build()
        );
    }

    /**
     * Retrieves all addresses for the specified customer.
     *
     * @param customerId the ID of the customer whose addresses are being retrieved
     * @return ResponseEntity containing the API response with the list of addresses
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAllAddresses(@PathVariable Long customerId) {
        List<AddressDTO> addresses = addressService.getAllAddresses(customerId);
        String message = addresses.isEmpty() ? "No addresses found" : "Addresses fetched successfully";
        return ResponseEntity.ok(
                ApiResponse.<List<AddressDTO>>builder()
                        .status(true)
                        .message(message)
                        .data(addresses)
                        .build()
        );
    }
}