package org.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.backend.dto.common.ApiResponse;
import org.backend.dto.user.request.UserRegisterRequestDTO;
import org.backend.dto.user.request.UserUpdateRequestDTO;
import org.backend.dto.user.response.UserRegisterResponseDTO;
import org.backend.model.Customer;
import org.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user and customer operations.
 *
 * This controller provides endpoints for user registration, profile management,
 * and customer information retrieval. It handles HTTP requests related to user
 * and customer resources at the base path "/user".
 *
 * @author Stylo User Management Service
 * @version 1.0
 */
@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    /** Service layer for user-related business logic operations */
    private final UserService userService;

    /**
     * Registers a new user with the provided registration details.
     *
     * @param registerUser the user registration request containing user details
     * @return ResponseEntity containing the registered user's information wrapped in an ApiResponse
     * @throws jakarta.validation.ConstraintViolationException if the request body validation fails
     */
    @PostMapping("/register")
    ResponseEntity<ApiResponse<UserRegisterResponseDTO>> userRegister(@Valid @RequestBody UserRegisterRequestDTO registerUser) {

       UserRegisterResponseDTO response = userService.userRegister(registerUser);
        //UserRegisterResponseDTO response = userService.completeProfile(registerUser);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<UserRegisterResponseDTO>builder()
                        .status(true)
                        .message("User with mobile " + response.getMobile() + " has been successfully created!")
                        .data(response)
                        .build());
    }

    /**
     * Updates an existing user's information.
     *
     * @param userId the ID of the user to be updated
     * @param updateRequestDTO the update request containing new user details
     * @return ResponseEntity containing the updated user information wrapped in an ApiResponse
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserRegisterResponseDTO>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequestDTO updateRequestDTO) {
        UserRegisterResponseDTO response = userService.updateUser(userId, updateRequestDTO);
        return ResponseEntity.ok(
                ApiResponse.<UserRegisterResponseDTO>builder()
                        .status(true)
                        .message("User updated successfully")
                        .data(response)
                        .build()
        );
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param userId the ID of the user to retrieve
     * @return ResponseEntity containing the user information wrapped in an ApiResponse
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserRegisterResponseDTO>> getUserById(@PathVariable Long userId) {

        UserRegisterResponseDTO response = userService.getUserById(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<UserRegisterResponseDTO>builder()
                        .status(true)
                        .message("User with the give userId: " + userId + " has been fetched successfully!")
                        .data(response)
                        .build());
    }

    /**
     * Retrieves all users from the system.
     *
     * Note: This endpoint is currently unrestricted but may be restricted to ADMIN role
     * in future versions (see commented @PreAuthorize annotation).
     *
     * @return ResponseEntity containing a list of all users wrapped in an ApiResponse
     */
    //@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserRegisterResponseDTO>>> getAllUsers() {

        List<UserRegisterResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.<List<UserRegisterResponseDTO>>builder()
                        .status(true)
                        .message("All users fetched successfully")
                        .data(users)
                        .build()
        );
    }

    /**
     * Retrieves a customer by their unique identifier.
     *
     * @param customerId the ID of the customer to retrieve
     * @return ResponseEntity containing the customer information wrapped in an ApiResponse
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(
            @PathVariable Long customerId) {
        Customer response = userService.getCustomerById(customerId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<Customer>builder()
                        .status(true)
                        .message("Customer fetched successfully")
                        .data(response)
                        .build());
    }

    /**
     * Retrieves all customers from the system.
     *
     * @return ResponseEntity containing a list of all customers wrapped in an ApiResponse
     */
    @GetMapping("/customer/all")
    public ResponseEntity<ApiResponse<List<UserRegisterResponseDTO>>> getAllCustomers() {
        List<UserRegisterResponseDTO> customers = userService.getAllCustomers();
        return ResponseEntity.ok(
                ApiResponse.<List<UserRegisterResponseDTO>>builder()
                        .status(true)
                        .message("All customers fetched successfully")
                        .data(customers)
                        .build()
        );
    }
}
