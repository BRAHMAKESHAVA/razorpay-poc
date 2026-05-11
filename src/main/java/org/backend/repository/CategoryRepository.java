package org.backend.repository;

import org.backend.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<ServiceCategory, Long> {

    //@Query("SELECT c FROM ServiceCategory c WHERE c.salonId = :salonId AND c.isActive = true")
    List<ServiceCategory> findBySalonId(Long salonId);

    //@Query("SELECT c FROM ServiceCategory c WHERE c.isActive = true")
    List<ServiceCategory> findAll();

    //@Query("SELECT c FROM ServiceCategory c WHERE c.categoryId = :categoryId AND c.salonId = :salonId AND c.isActive = true")
    Optional<ServiceCategory> findByCategoryIdAndSalonId(Long categoryId, Long salonId);

    // Check if a category with the same name exists for the salon (case-insensitive)
    boolean existsBySalonIdAndCategoryNameIgnoreCase(Long salonId, String categoryName);

    // Check for duplicates when updating: same name but different ID
    boolean existsBySalonIdAndCategoryNameIgnoreCaseAndCategoryIdNot(Long salonId, String categoryName, Long categoryId);

}