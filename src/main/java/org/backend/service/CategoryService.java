package org.backend.service;

import lombok.RequiredArgsConstructor;
import org.backend.exception.BadRequestException;
import org.backend.exception.DuplicateResourceException;
import org.backend.exception.ResourceNotFoundException;
import org.backend.model.ServiceCategory;
import org.backend.repository.CategoryRepository;
import org.backend.repository.SalonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing service categories.
 * Provides business logic for creating, updating, deleting, and retrieving service categories
 * associated with salons, including validation for uniqueness and existence.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SalonRepository salonRepository;

    /**
     * Creates a new category for a salon.
     * Validates salon existence and ensures category name uniqueness within the salon.
     *
     * @param category the category details to create
     * @return the created ServiceCategory
     * @throws ResourceNotFoundException if salon not found
     * @throws DuplicateResourceException if category name already exists for the salon
     */
    public ServiceCategory createCategory(ServiceCategory category) {

        // Ensure the salon exists before creating a category
        salonRepository.findById(category.getSalonId())
                .orElseThrow(() -> new ResourceNotFoundException("Salon not found with id: " + category.getSalonId()));

        // Normalize the category name
        String categoryName = category.getCategoryName().trim();

        // Check for duplicate category names within the same salon
        boolean exists = categoryRepository.existsBySalonIdAndCategoryNameIgnoreCase(category.getSalonId(), categoryName);

        if (exists) throw new DuplicateResourceException("Category '" + categoryName + "' already exists for this salon");

        // Assign normalized name and save
        category.setCategoryName(categoryName);
        if (category.getIsActive() == null) category.setIsActive(true);
        return categoryRepository.save(category);
    }

    /**
     * Updates an existing category.
     * Allows updating category name and active status, with validation for uniqueness.
     *
     * @param id the ID of the category to update
     * @param request the update request containing new details
     * @return the updated ServiceCategory
     * @throws BadRequestException if salon ID is missing or category name is empty
     * @throws ResourceNotFoundException if category not found for the salon
     * @throws DuplicateResourceException if new category name already exists
     */
    public ServiceCategory updateCategory(Long id, ServiceCategory request) {
        if (request.getSalonId() == null) {
            throw new BadRequestException("Salon ID is required to update the category.");
        }
        // Ensure the category exists and belongs to the specified salon
        ServiceCategory category = categoryRepository.findByCategoryIdAndSalonId(id, request.getSalonId())
                .orElseThrow(() -> new ResourceNotFoundException("Category with ID:" + id + " was not found for salon:" +request.getSalonId()));
        // Update category name if provided, ensuring uniqueness
        if (request.getCategoryName() != null) {
            String categoryName = request.getCategoryName().trim();
            if (categoryName.isEmpty()) throw new BadRequestException("Category name cannot be empty. Please provide a valid name.");
            boolean exists = categoryRepository
                    .existsBySalonIdAndCategoryNameIgnoreCaseAndCategoryIdNot(
                            category.getSalonId(),
                            categoryName,
                            category.getCategoryId()
                    );
            if (exists) throw new DuplicateResourceException("Category name already in use. Please use a different name.");
            if (category.getIsActive() == null) category.setIsActive(true);
            category.setCategoryName(categoryName);
        }

        // Update active status if provided
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());
        return categoryRepository.save(category);
    }

    /**
     * Deletes a category by its ID.
     * Ensures the category exists and belongs to the specified salon before deletion.
     *
     * @param salonId the ID of the salon
     * @param categoryId the ID of the category to delete
     * @throws ResourceNotFoundException if category not found for the salon
     */
    public void deleteCategory(Long salonId, Long categoryId) {
        // Ensure the category exists and belongs to the specified salon
        ServiceCategory category = categoryRepository.findByCategoryIdAndSalonId(categoryId, salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for this salon"));
        categoryRepository.deleteById(categoryId);
    }

    /**
     * Retrieves all categories.
     *
     * @return list of all ServiceCategory
     */
    public List<ServiceCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Retrieves all categories belonging to a specific salon.
     *
     * @param salonId the ID of the salon
     * @return list of ServiceCategory for the salon
     * @throws ResourceNotFoundException if salon not found
     */
    public List<ServiceCategory> getCategoriesBySalon(Long salonId) {
        // Ensure the salon exists before creating a category
        salonRepository.findById(salonId).orElseThrow(() -> new ResourceNotFoundException("Salon not found with id: " + salonId));
        return categoryRepository.findBySalonId(salonId);
    }
}
