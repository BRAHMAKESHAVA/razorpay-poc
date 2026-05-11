package org.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.backend.dto.common.ApiResponse;
import org.backend.model.SalonResource;
import org.backend.service.SalonResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing salon resources.
 * This controller provides endpoints for creating, updating, deleting, and retrieving
 * salon resources. All endpoints operate under the base path "/api/salon-resources".
 */
@RestController
@RequestMapping("/api/salon-resources")
@RequiredArgsConstructor
public class SalonResourceController {

    private final SalonResourceService resourceService;

    /**
     * Creates a new salon resource.
     *
     * @param request the salon resource details to be created
     * @return ResponseEntity containing the API response with the created resource data
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SalonResource>> createResource(@Valid @RequestBody SalonResource request) {
        SalonResource created = resourceService.createResource(request);

        return ResponseEntity.ok(
                ApiResponse.<SalonResource>builder()
                        .status(true)
                        .message("Salon resource created successfully.")
                        .data(created)
                        .build()
        );
    }

    /**
     * Updates an existing salon resource by ID.
     *
     * @param id the ID of the resource to be updated
     * @param request the updated resource details
     * @return ResponseEntity containing the API response with the updated resource data
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalonResource>> updateResource(@PathVariable Long id, @RequestBody SalonResource request) {

        SalonResource updated = resourceService.updateResource(id, request);

        return ResponseEntity.ok(
                ApiResponse.<SalonResource>builder()
                        .status(true)
                        .message("Salon resource updated successfully.")
                        .data(updated)
                        .build()
        );
    }

    /**
     * Retrieves a salon resource by salon ID.
     *
     * @param salonId the ID of the salon whose resource is being retrieved
     * @return ResponseEntity containing the API response with the resource data
     */
    @GetMapping("/salon/{salonId}")
    public ResponseEntity<ApiResponse<SalonResource>> getResourceBySalonId(@PathVariable Long salonId) {
        SalonResource resource = resourceService.getResourceBySalonId(salonId);

        return ResponseEntity.ok(
                ApiResponse.<SalonResource>builder()
                        .status(true)
                        .message("Salon resource fetched successfully.")
                        .data(resource)
                        .build()
        );
    }

    /**
     * Deletes a salon resource by salon ID and resource ID.
     *
     * @param salonId the ID of the salon
     * @param resourceId the ID of the resource to be deleted
     * @return ResponseEntity containing the API response confirming the deletion
     */
    @DeleteMapping("/{salonId}/resources/{resourceId}")
    public ResponseEntity<ApiResponse<Void>> deleteResource(
            @PathVariable Long salonId,
            @PathVariable Long resourceId) {

        resourceService.deleteResource(salonId, resourceId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(true)
                        .message("Salon resource deleted successfully.")
                        .data(null)
                        .build()
        );
    }
}
