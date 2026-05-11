package org.backend.controller;

import lombok.RequiredArgsConstructor;
import org.backend.dto.*;
import org.backend.dto.common.ApiResponse;
import org.backend.dto.common.PageResponse;
import org.backend.service.SalonSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for searching salons based on location and services.
 * This controller provides endpoints for finding nearby salons and searching salons
 * that offer specific services. All endpoints operate under the base path "/api/search".
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SalonSearchController {

    private final SalonSearchService salonSearchService;

    /**
     * Retrieves nearby salons based on geographical coordinates and distance.
     * Supports optional pagination for large result sets.
     *
     * @param latitude the latitude of the search location
     * @param longitude the longitude of the search location
     * @param distance the search radius distance
     * @param unit the unit of distance (e.g., "KM" for kilometers)
     * @param page the page number for pagination (optional)
     * @param size the page size for pagination (optional)
     * @return ResponseEntity containing the API response with nearby salons data
     */
    @GetMapping("/salon/nearby")
    public ResponseEntity<?> getNearbySalons(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double distance,
            @RequestParam(defaultValue = "KM") String unit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {

        // With pagination
        if (page != null && size != null) {
            PageResponse<SalonDetailsDTO> response = salonSearchService.findNearbySalonsWithPagination(
                            latitude, longitude, distance, unit, page, size);
            return ResponseEntity.ok(
                    ApiResponse.<PageResponse<SalonDetailsDTO>>builder()
                            .status(true)
                            .message("Nearby salons fetched successfully with pagination.")
                            .data(response)
                            .build()
            );
        }

        // Without pagination
        List<SalonDetailsDTO> salons = salonSearchService.findNearbySalons(latitude, longitude, distance, unit);
        String message = salons.isEmpty() ? "No salons found near your location." : "Nearby salons fetched successfully.";
        return ResponseEntity.ok(
                ApiResponse.<List<SalonDetailsDTO>>builder()
                        .status(true)
                        .message(message)
                        .data(salons)
                        .build()
        );
    }

    /**
     * Searches for salons that offer specific services within a geographical area.
     *
     * @param latitude the latitude of the search location
     * @param longitude the longitude of the search location
     * @param distance the search radius distance
     * @param unit the unit of distance (e.g., "KM" for kilometers)
     * @param request the request containing the list of service names to search for
     * @return ResponseEntity containing the API response with salons offering the selected services
     */
    @PostMapping("/salon/search-by-services")
    public ResponseEntity<ApiResponse<List<SalonSearchWithSelectedServicesResponseDTO>>> searchSalonsByServices(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double distance,
            @RequestParam String unit,
            @RequestBody SalonSearchWithSelectedServicesRequest request
    ) {
System.out.println(request);
        List<SalonSearchWithSelectedServicesResponseDTO> result =
                salonSearchService.findSalonsWithSelectedServices(
                        latitude,
                        longitude,
                        distance,
                        unit,
                        request.getServiceNames()
                );

        if (result.isEmpty())
            return ResponseEntity.ok(new ApiResponse<>(true, "No salons found for the selected service.", List.of()));

        return ResponseEntity.ok(new ApiResponse<>(true, "Salons fetched successfully", result)
        );
    }

    @GetMapping("/salons/nearby/suggestions")
    public ResponseEntity<?> searchNearbySalons(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double distance,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "KM") String unit
    ) {
        List<SalonDetailsDTO> salons =
                salonSearchService.searchNearbySalons(
                        latitude, longitude, distance, unit, keyword);

        return ResponseEntity.ok(
                ApiResponse.<List<SalonDetailsDTO>>builder()
                        .status(true)
                        .message("Search results fetched successfully")
                        .data(salons)
                        .build()
        );
    }

    @GetMapping("/salons/by-name")
    public ResponseEntity<?> getSalonsByName(
            @RequestParam String salonName,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "KM") String unit
    ) {

        List<SalonDetailsDTO> salons =
                salonSearchService.getSalonsByName(salonName, latitude, longitude, unit);

        return ResponseEntity.ok(
                ApiResponse.<List<SalonDetailsDTO>>builder()
                        .status(true)
                        .message("Salons fetched successfully")
                        .data(salons)
                        .build()
        );
    }
}