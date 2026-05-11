package org.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.backend.dto.CategoryGroupDTO;
import org.backend.dto.CategoryResponse;
import org.backend.dto.UpdateServiceRequest;
import org.backend.dto.common.ApiResponse;
import org.backend.model.SalonService;
import org.backend.service.SalonServices;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SalonServiceController {

    private final SalonServices serviceManager;

    // GET all services
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllServices() {

        List<CategoryResponse> services = serviceManager.getAllServices();

        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponse>>builder()
                        .status(true)
                        .message("Services fetched successfully")
                        .data(services)
                        .build()
        );
    }

    // Create Service
    @PostMapping
    public ResponseEntity<?> createService(@Valid @RequestBody SalonService request){

        SalonService created = serviceManager.createService(request);

        return ResponseEntity.ok(
                ApiResponse.<SalonService>builder()
                        .status(true)
                        .message("Service created successfully.")
                        .data(created)
                        .build()
        );
    }

    // Update Service
    @PutMapping("/{serviceId}")
    public ResponseEntity<?> updateService(@PathVariable Long serviceId, @Valid @RequestBody UpdateServiceRequest request){

        SalonService updated = serviceManager.updateService(serviceId, request);

        return ResponseEntity.ok(
                ApiResponse.<SalonService>builder()
                        .status(true)
                        .message("Service updated successfully.")
                        .data(updated)
                        .build()
        );
    }

    // Get Services of a Salon (with & without pagination)
    @GetMapping("/salon/{salonId}")
    public ResponseEntity<?> getServicesBySalon(@PathVariable Long salonId,
                                         @RequestParam(required = false) Integer pageNo,
                                         @RequestParam(required = false) Integer pageSize) {

        // With pagination
        if (pageNo != null && pageSize != null) {

            Page<SalonService> page = serviceManager.getServicesBySalon(salonId, pageNo, pageSize);

            String message = page.isEmpty()
                    ? "No services found for this salon."
                    : "Services fetched successfully with pagination.";

            return ResponseEntity.ok(
                    ApiResponse.<Page<SalonService>>builder()
                            .status(true)
                            .message(message)
                            .data(page)
                            .build()
            );
        }

        // Without pagination
        List<CategoryGroupDTO> services = serviceManager.getServicesBySalon(salonId);

        String message = services.isEmpty()
                ? "No services found for this salon."
                : "Services fetched successfully.";

        return ResponseEntity.ok(
                ApiResponse.<List<CategoryGroupDTO>>builder()
                        .status(true)
                        .message(message)
                        .data(services)
                        .build()
        );
    }

    // Get Services By Salon & Category
    @GetMapping("/salon/{salonId}/categories/{categoryId}")
    public ResponseEntity<?> getServicesByCategoryAnsSalon(@PathVariable Long salonId,
                                                           @PathVariable Long categoryId){

        List<SalonService> services =
                serviceManager.getServicesByCategoryAndSalon(salonId, categoryId);

        String message = services.isEmpty()
                ? "No services found for this category in the salon."
                : "Services fetched successfully for the selected category.";

        return ResponseEntity.ok(
                ApiResponse.<List<SalonService>>builder()
                        .status(true)
                        .message(message)
                        .data(services)
                        .build()
        );
    }

    // Get Service By ID
    @GetMapping("/{serviceId}")
    public ResponseEntity<?> getServiceById(@PathVariable Long serviceId){

        SalonService service = serviceManager.getServiceById(serviceId);

        return ResponseEntity.ok(
                ApiResponse.<SalonService>builder()
                        .status(true)
                        .message("Service details fetched successfully.")
                        .data(service)
                        .build()
        );
    }
}