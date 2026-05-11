package org.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.backend.dto.common.ApiResponse;
import org.backend.model.ServiceCategory;
import org.backend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing salon service categories.
 * This controller provides endpoints for creating, updating, deleting, and retrieving
 * service categories associated with salons. All endpoints operate under the base path "/api/service-categories".
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service-categories")
public class SalonCategoryController {

    private final CategoryService categoryService;

    /**
     * Creates a new service category.
     *
     * @param category the service category details to be created
     * @return ResponseEntity containing the API response with the created category data
     */
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody ServiceCategory category){

        ServiceCategory created = categoryService.createCategory(category);

        return ResponseEntity.ok(
                ApiResponse.<ServiceCategory>builder()
                        .status(true)
                        .message("Service category created successfully.")
                        .data(created)
                        .build()
        );
    }

    /**
     * Updates an existing service category.
     *
     * @param categoryId the ID of the category to be updated
     * @param category the updated category details
     * @return ResponseEntity containing the API response with the updated category data
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId, @RequestBody ServiceCategory category){

        ServiceCategory updated = categoryService.updateCategory(categoryId, category);

        return ResponseEntity.ok(
                ApiResponse.<ServiceCategory>builder()
                        .status(true)
                        .message("Service category updated successfully.")
                        .data(updated)
                        .build()
        );
    }

    /**
     * Deletes a service category for a specific salon.
     *
     * @param salonId the ID of the salon
     * @param categoryId the ID of the category to be deleted
     * @return ResponseEntity containing the API response confirming the deletion
     */
    @DeleteMapping("/{salonId}/category/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long salonId, @PathVariable Long categoryId){

        categoryService.deleteCategory(salonId,categoryId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(true)
                        .message("Service category deleted successfully.")
                        .data(null)
                        .build()
        );
    }

    /**
     * Retrieves all service categories for a specific salon.
     *
     * @param salonId the ID of the salon whose categories are being retrieved
     * @return ResponseEntity containing the API response with the list of categories
     */
    @GetMapping("/salon/{salonId}")
    public ResponseEntity<?> getCategoryBySalonId(@PathVariable Long salonId){

        List<ServiceCategory> categories = categoryService.getCategoriesBySalon(salonId);

        String message = categories.isEmpty()
                ? "No service categories found for this salon."
                : "Service categories fetched successfully for the salon.";

        return ResponseEntity.ok(
                ApiResponse.<List<ServiceCategory>>builder()
                        .status(true)
                        .message(message)
                        .data(categories)
                        .build()
        );
    }

    /**
     * Retrieves all service categories.
     *
     * @return ResponseEntity containing the API response with the list of all categories
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ServiceCategory>>> getAllCategories() {
        List<ServiceCategory> categories = categoryService.getAllCategories();
        ApiResponse<List<ServiceCategory>> response = ApiResponse.<List<ServiceCategory>>builder()
                .status(true)
                .message("Categories fetched successfully")
                .data(categories)
                .build();
        return ResponseEntity.ok(response);
    }

}