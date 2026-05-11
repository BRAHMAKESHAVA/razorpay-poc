package org.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.backend.dto.common.AddressDTO;
import org.backend.exception.BadRequestException;
import org.backend.exception.ResourceNotFoundException;
import org.backend.model.Address;
import org.backend.repository.AddressRepository;
import org.backend.repository.CustomerRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.backend.enums.CountryCode.validateCountryCode;

/**
 * Service class for managing customer addresses.
 * Provides business logic for creating, updating, deleting, and retrieving customer addresses,
 * including validation and default address management.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Value("${address.max.count}")
    private int maxAddressCount;

    /**
     * Creates a new address for the specified customer.
     * Validates customer existence, address count limits, and country code.
     * Handles default address logic by resetting other defaults if this is set as default.
     *
     * @param customerId the ID of the customer
     * @param address the address details to create
     * @return the created address as AddressDTO
     * @throws ResourceNotFoundException if customer not found
     * @throws BadRequestException if maximum address count exceeded
     */
    public AddressDTO createAddress(Long customerId, Address address) {
        if (!customerRepository.existsByCustomerId(customerId)) throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        // Check max address limit
        long count = addressRepository.countByCustomerId(customerId);
        if (count >= maxAddressCount) throw new BadRequestException("Maximum " + maxAddressCount + " addresses allowed per customer");
        validateCountryCode(address.getCountryCode());
        if (address.getIsDefault() == null) address.setIsDefault(false);
        // If new address is default → reset old default
        if (address.getIsDefault()) addressRepository.resetDefaultForCustomer(customerId);
        address.setCustomerId(customerId);
        AddressDTO addressDTO = new AddressDTO();
        BeanUtils.copyProperties(addressRepository.save(address), addressDTO);
        return addressDTO;
    }

    /**
     * Updates an existing address for the specified customer.
     * Only updates non-null fields from the DTO.
     *
     * @param customerId the ID of the customer
     * @param addressId the ID of the address to update
     * @param dto the address update details
     * @return the updated address as AddressDTO
     * @throws ResourceNotFoundException if customer or address not found
     */
    public AddressDTO updateAddress(Long customerId, Long addressId, AddressDTO dto) {
        if (!customerRepository.existsById(customerId)) throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        Address address = addressRepository
                .findByAddressIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for this customer"));
        if (dto.getCustomerName() != null) address.setCustomerName(dto.getCustomerName());
        if (dto.getHouseNumber() != null) address.setHouseNumber(dto.getHouseNumber());
        if (dto.getBuildingName() != null) address.setBuildingName(dto.getBuildingName());
        if (dto.getArea() != null) address.setArea(dto.getArea());
        if (dto.getLandmark() != null) address.setLandmark(dto.getLandmark());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getState() != null) address.setState(dto.getState());
        if (dto.getCountryCode() != null){
            validateCountryCode(dto.getCountryCode());
            address.setCountryCode(dto.getCountryCode());
        }
        if (dto.getPinCode() != null) address.setPinCode(dto.getPinCode());
        if (dto.getLatitude() != null) address.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) address.setLongitude(dto.getLongitude());
        if (dto.getAddressType() != null) address.setAddressType(dto.getAddressType());
        if (dto.getLabelName() != null) address.setLabelName(dto.getLabelName());
        if (dto.getIsDefault() != null && dto.getIsDefault()){
            addressRepository.resetDefaultForCustomer(customerId, addressId);
            address.setIsDefault(dto.getIsDefault());
        }
        AddressDTO addressDTO = new AddressDTO();
        BeanUtils.copyProperties(addressRepository.save(address), addressDTO);
        return addressDTO;
    }

    /**
     * Deletes an address for the specified customer.
     * Prevents deletion of default addresses.
     *
     * @param customerId the ID of the customer
     * @param addressId the ID of the address to delete
     * @throws ResourceNotFoundException if customer or address not found
     * @throws BadRequestException if trying to delete a default address
     */
    public void deleteAddress(Long customerId, Long addressId) {
        if (!customerRepository.existsById(customerId)) throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        Address address = addressRepository.findByAddressIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for this customer"));
        if (address.getIsDefault()) throw new BadRequestException("Default address cannot be deleted");
        addressRepository.delete(address);
    }

    /**
     * Retrieves a specific address by ID for the specified customer.
     *
     * @param customerId the ID of the customer
     * @param addressId the ID of the address to retrieve
     * @return the address as AddressDTO
     * @throws ResourceNotFoundException if customer or address not found
     */
    public AddressDTO getAddressById(Long customerId, Long addressId) {
        if (!customerRepository.existsById(customerId)) throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        Address address = addressRepository.findByAddressIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found for the given customerId and addressId"));
        AddressDTO addressDTO = new AddressDTO();
        BeanUtils.copyProperties(address, addressDTO);
        return addressDTO;
    }

    /**
     * Retrieves all addresses for the specified customer.
     *
     * @param customerId the ID of the customer
     * @return list of addresses as AddressDTO
     * @throws ResourceNotFoundException if customer not found
     */
    public List<AddressDTO> getAllAddresses(Long customerId) {
        if (!customerRepository.existsById(customerId)) throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        return addresses.stream().map(address -> {
            AddressDTO dto = new AddressDTO();
            BeanUtils.copyProperties(address, dto);
            return dto;
        }).toList();
    }
}