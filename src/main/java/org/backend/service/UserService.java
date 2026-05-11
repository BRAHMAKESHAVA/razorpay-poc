package org.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.backend.dto.user.request.UserRegisterRequestDTO;
import org.backend.dto.user.request.UserUpdateRequestDTO;
import org.backend.dto.user.response.UserRegisterResponseDTO;
import org.backend.enums.Role;
import org.backend.exception.BadRequestException;
import org.backend.exception.DuplicateResourceException;
import org.backend.exception.ResourceNotFoundException;
import org.backend.model.Customer;
import org.backend.model.Users;
import org.backend.repository.CustomerRepository;
import org.backend.repository.PartnerRepository;
import org.backend.repository.UserRepository;
import org.backend.utill.JwtUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing user and customer operations.
 * Handles user registration, updates, and retrieval, including role-based validations.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PartnerRepository partnerRepository;
    private final JwtUtill jwtUtill;
    private final HttpServletRequest httpRequest;
    private final AuthService authService;


    /**
     * Registers a new user based on the provided registration details.
     * The method performs the following steps:
     * 1. Checks if the mobile number already exists in the system. If it does, a DuplicateResourceException is thrown.
     * 2. If the user role is PARTNER, it verifies if the mobile number
     * belongs to an onboard partner. If not, a BadRequestException is thrown.
     * 3. If the checks pass, a new Users entity is created and saved to
     * the database. The password is encoded before saving.
     * 4. If the user role is CUSTOMER, a corresponding Customer entity is created and
     * saved to the database, linking it to the newly created Users entity.
     * 5. Finally, a UserRegisterResponseDTO is created by copying the properties from
     * the saved Users entity and returned as the response.
     *
     * @param registerUser The DTO containing user registration details.
     * @return A DTO containing the registered user's information.
     * @throws DuplicateResourceException If the mobile number already exists.
     * @throws BadRequestException If a partner tries to register without being an onboard partner.
     */
    public UserRegisterResponseDTO userRegister(UserRegisterRequestDTO registerUser) {

        if (userRepository.existsByMobile(registerUser.getMobile())) {
            throw new DuplicateResourceException("This mobile number is already registered. Please log in instead.");
        }

        if (registerUser.getRole() == Role.PARTNER) {
            boolean partnerExists = partnerRepository.existsByMobile(registerUser.getMobile());
            if (!partnerExists) {
                throw new BadRequestException("Only our onboard partners can register from this platform.");
            }
        }

        Users users = new Users();
        BeanUtils.copyProperties(registerUser, users);

        users.setAge(Integer.parseInt(registerUser.getAge())); // Convert age from String to Integer
        users = userRepository.save(users);

        if (registerUser.getRole() == Role.CUSTOMER) {
            customerRepository.save(Customer.builder().users(users).build());
        }

        UserRegisterResponseDTO response = new UserRegisterResponseDTO();
        BeanUtils.copyProperties(users, response);
        return response;
    }

    /**
     * Updates an existing user's information based on the provided user ID and update details.
     * The method performs the following steps:
     * 1. Retrieves the existing user from the database using the provided ID. If the user is not found, a ResourceNotFoundException is thrown.
     * 2. For each field in the UserUpdateRequestDTO, it checks if the field is not null and not blank (for String fields). If so, it updates the corresponding field in the existing Users entity.
     * 3. After updating the necessary fields, it saves the updated Users entity back to the database.
     * 4. Finally, a UserRegisterResponseDTO is created by copying the properties from the updated Users entity and returned as the response.
     *
     * @param id The ID of the user to be updated.
     * @param user The DTO containing the user update details.
     * @return A DTO containing the updated user's information.
     * @throws ResourceNotFoundException If no user is found with the provided ID.
     */
    public UserRegisterResponseDTO updateUser(Long id, UserUpdateRequestDTO user) {

        Users existing = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            existing.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null && !user.getLastName().isBlank()) {
            existing.setLastName(user.getLastName());
        }
        if (user.getGender() != null && !user.getGender().isBlank()) {
            existing.setGender(user.getGender());
        }
        if (user.getAge() != null) {
            existing.setAge(user.getAge());
        }
        if (user.getEmail() != null) {
            existing.setEmail(user.getEmail());
        }

        Users updated = userRepository.save(existing);
        UserRegisterResponseDTO response = new UserRegisterResponseDTO();
        BeanUtils.copyProperties(updated, response);
        return response;
    }
    /**
     * Retrieves a user's information based on the provided user ID.
     * The method performs the following steps:
     * 1. Retrieves the user from the database using the provided ID. If the user is not found, a ResourceNotFoundException is thrown.
     * 2. A UserRegisterResponseDTO is created by copying the properties from the retrieved Users entity.
     * 3. The UserRegisterResponseDTO is returned as the response.
     *
     * @param id The ID of the user to be retrieved.
     * @return A DTO containing the user's information.
     * @throws ResourceNotFoundException If no user is found with the provided ID.
     */
    public UserRegisterResponseDTO getUserById(Long id) {
        Users user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        UserRegisterResponseDTO response = new UserRegisterResponseDTO();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    /**
     * Retrieves a list of all users in the system.
     * The method performs the following steps:
     * 1. Retrieves all Users entities from the database.
     * 2. For each Users entity, a UserRegisterResponseDTO is created by copying the properties from the Users entity.
     * 3. A list of UserRegisterResponseDTOs is returned as the response.
     *
     * @return A list of DTOs containing information about all users.
     */
    public List<UserRegisterResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserRegisterResponseDTO dto = new UserRegisterResponseDTO();
            BeanUtils.copyProperties(user, dto);
            return dto;
        }).toList();
    }

    /**
     * Retrieves a customer's information based on the provided customer ID.
     * The method performs the following steps:
     * 1. Retrieves the Customer entity from the database using the provided ID. If the customer is not found, a ResourceNotFoundException is thrown.
     * 2. From the retrieved Customer entity, it gets the associated Users entity.
     * 3. A UserRegisterResponseDTO is created by copying the properties from the associated Users entity.
     * 4. The UserRegisterResponseDTO is returned as the response.
     *
     * @param id The ID of the customer to be retrieved.
     * @return A DTO containing the customer's information.
     * @throws ResourceNotFoundException If no customer is found with the provided ID.
     */
    public Customer getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        //Users user = customer.getUsers();
        //UserRegisterResponseDTO response = new UserRegisterResponseDTO();
        //BeanUtils.copyProperties(user, response);
        return customer;
    }

    //fetch all customers
    /**
     * Retrieves a list of all customers in the system.
     *
     * @return a list of DTOs containing information about all customers
     */
    public List<UserRegisterResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream().map(customer -> {
            UserRegisterResponseDTO dto = new UserRegisterResponseDTO();
            BeanUtils.copyProperties(customer.getUsers(), dto);
            return dto;
        }).toList();
    }
}