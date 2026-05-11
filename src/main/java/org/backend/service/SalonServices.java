package org.backend.service;

import lombok.RequiredArgsConstructor;
import org.backend.dto.CategoryGroupDTO;
import org.backend.dto.CategoryResponse;
import org.backend.dto.CategoryServiceDTO;
import org.backend.dto.UpdateServiceRequest;
import org.backend.exception.BadRequestException;
import org.backend.exception.DuplicateResourceException;
import org.backend.exception.ResourceNotFoundException;
import org.backend.model.SalonService;
import org.backend.model.ServiceCategory;
import org.backend.repository.CategoryRepository;
import org.backend.repository.SalonRepository;
import org.backend.repository.SalonServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing salon services.
 * Provides business logic for creating, updating, retrieving, and managing salon services,
 * including grouping by categories and pagination support.
 */
@Service
@RequiredArgsConstructor
public class SalonServices {

    private final SalonServiceRepository salonServiceRepository;
    private final SalonRepository salonRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Fetches all services for a given salon, grouped by category.
     *
     * @param salonId the ID of the salon
     * @return a list of CategoryGroupDTO containing services grouped by category
     * @throws ResourceNotFoundException if salon not found or no services exist
     */
    public List<CategoryGroupDTO> getServicesBySalon(Long salonId) {
        if (!salonRepository.existsById(salonId)) {
            throw new ResourceNotFoundException("Salon not found");
        }

        // Build a map of categoryId -> categoryName for lookup
        List<ServiceCategory> categories = categoryRepository.findAll();

        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(
                        ServiceCategory::getCategoryId,
                        ServiceCategory::getCategoryName
                ));

        // Retrieve all services for the salon
        List<SalonService> services =
                salonServiceRepository.findBySalonId(salonId);

        if (services.isEmpty()) {
            throw new ResourceNotFoundException("No services found for this salon");
        }

        // Group services by categoryId
        Map<Long, List<SalonService>> groupedByCategory =
                services.stream()
                        .collect(Collectors.groupingBy(
                                SalonService::getCategoryId
                        ));

        // Convert grouped data into DTOs
        return groupedByCategory.entrySet().stream()
                .map(entry -> new CategoryGroupDTO(
                        entry.getKey(),
                        categoryMap.get(entry.getKey()),
                        entry.getValue()
                ))
                .toList();
    }

    /**
     * Creates a new service for a salon.
     * Validates salon and category existence, and ensures service name uniqueness.
     *
     * @param request the SalonService object containing service details
     * @return the created SalonService
     * @throws ResourceNotFoundException if salon or category not found
     * @throws DuplicateResourceException if service name already exists for the salon
     */
    public SalonService createService(SalonService request) {
        // Validate salon existence
        salonRepository.findById(request.getSalonId())
                .orElseThrow(() -> new ResourceNotFoundException("Salon not found"));

        // Validate category existence
        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Ensure service name is unique within the salon
        boolean exists = salonServiceRepository
                .existsBySalonIdAndServiceNameIgnoreCase(
                        request.getSalonId(),
                        request.getServiceName().trim()
                );

        if (exists) {
            throw new DuplicateResourceException(
                    "Service '" + request.getServiceName() + "' already exists for this salon"
            );
        }

        if (request.getIsActive() == null) request.setIsActive(true);

        return salonServiceRepository.save(request);
    }

    /**
     * Updates an existing service.
     * Allows updating service name, duration, buffer, price, and active status with validation.
     *
     * @param serviceId the ID of the service to update
     * @param request the UpdateServiceRequest containing update details
     * @return the updated SalonService
     * @throws BadRequestException if salon ID is missing
     * @throws ResourceNotFoundException if service not found for the salon
     * @throws DuplicateResourceException if new service name already exists
     */
    public SalonService updateService(Long serviceId, UpdateServiceRequest request) {
        if (request.getSalonId() == null) {
            throw new BadRequestException("Salon ID is required to update the service.");
        }

        SalonService service =
                salonServiceRepository.findByServiceIdAndSalonId(
                                serviceId,
                                request.getSalonId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service with ID " + serviceId + " not found for salon ID " + request.getSalonId()
                                )
                        );

        // Update service name with duplicate check
        if (request.getServiceName() != null) {
            String serviceName = request.getServiceName().trim();

            boolean exists = salonServiceRepository
                    .existsBySalonIdAndServiceNameIgnoreCaseAndServiceIdNot(
                            service.getSalonId(),
                            serviceName,
                            service.getServiceId()
                    );

            if (exists) {
                throw new DuplicateResourceException(
                        "Service name already in use. Please choose a different name."
                );
            }

            service.setServiceName(serviceName);
        }

        // Update other fields if provided
        if (request.getDurationMinutes() != null) {
            service.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getBufferMinutes() != null) {
            service.setBufferMinutes(request.getBufferMinutes());
        }

        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }

        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }

        return salonServiceRepository.save(service);
    }

    /**
     * Retrieves all services grouped by category.
     * Fetches services from the repository and groups them by category name.
     *
     * @return a list of CategoryResponse with services grouped by category
     */
    public List<CategoryResponse> getAllServices() {
        List<CategoryServiceDTO> services =
                salonServiceRepository.fetchCategoryAndServices();

        Map<String, Set<String>> grouped =
                services.stream()
                        .collect(Collectors.groupingBy(
                                //dto -> dto.getCategoryName().trim(),
                                dto -> dto.getCategoryName().trim().toLowerCase(),
                                //Collectors.mapping(dto -> dto.getServiceName().trim(), Collectors.toSet())
                                Collectors.mapping(
                                        dto -> dto.getServiceName().trim().toLowerCase(),
                                        Collectors.toSet()
                                )
                        ));

        return grouped.entrySet().stream()
                .map(entry -> new CategoryResponse(
                        capitalize(entry.getKey()),
                        //entry.getValue().stream().toList()
                        entry.getValue().stream().map(this::capitalize).toList()
                ))
                .toList();
    }

    /**
     * Fetches a single service by its ID.
     *
     * @param serviceId the ID of the service
     * @return the SalonService object
     * @throws ResourceNotFoundException if service not found
     */
    public SalonService getServiceById(Long serviceId) {
        return salonServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
    }

    /**
     * Fetches services for a given salon and category.
     *
     * @param salonId the ID of the salon
     * @param categoryId the ID of the category
     * @return a list of SalonService for the salon and category
     * @throws ResourceNotFoundException if salon or category not found
     */
    public List<SalonService> getServicesByCategoryAndSalon(Long salonId, Long categoryId) {
        salonRepository.findById(salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Salon not found"));

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return salonServiceRepository.findBySalonIdAndCategoryId(
                salonId,
                categoryId
        );
    }

    /**
     * Fetches paginated services for a given salon.
     *
     * @param salonId the ID of the salon
     * @param pageNo the page number (0-based)
     * @param pageSize the number of items per page
     * @return a Page of SalonService
     * @throws ResourceNotFoundException if salon not found
     */
    public Page<SalonService> getServicesBySalon(Long salonId, int pageNo, int pageSize) {
        salonRepository.findById(salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Salon not found"));

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        return salonServiceRepository.findBySalonId(salonId, pageable);
    }

    /**
     * Helper method to capitalize each word in a string.
     *
     * @param str the input string
     * @return the capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;

        return Arrays.stream(str.split(" "))
                .map(word ->
                        word.substring(0, 1).toUpperCase() + word.substring(1)
                )
                .collect(Collectors.joining(" "));
    }
}