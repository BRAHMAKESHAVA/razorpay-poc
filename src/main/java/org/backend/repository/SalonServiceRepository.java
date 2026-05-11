package org.backend.repository;

import org.backend.dto.CategoryServiceDTO;
import org.backend.model.SalonResource;
import org.backend.model.SalonService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {

    // Check if a service with the same name exists for the salon (case-insensitive)
    boolean existsBySalonIdAndServiceNameIgnoreCase(Long salonId, String serviceName);

    // Check for duplicates when updating (exclude current service ID)
    boolean existsBySalonIdAndServiceNameIgnoreCaseAndServiceIdNot(Long salonId, String serviceName, Long serviceId);

    @Query("SELECT DISTINCT new org.backend.dto.CategoryServiceDTO(c.categoryName, s.serviceName) FROM SalonService s JOIN ServiceCategory c ON s.categoryId = c.categoryId WHERE s.isActive = true AND c.isActive = true")
    List<CategoryServiceDTO> fetchCategoryAndServices();

    @Query("SELECT s FROM SalonService s WHERE s.salonId IN :salonIds AND s.isActive = true")
    List<SalonService> findBySalonIds(@Param("salonIds") List<Long> salonIds);

    //@Query("SELECT s FROM SalonService s WHERE s.salonId = :salonId AND s.isActive = true")
    List<SalonService> findBySalonId(Long salonId);

    //@Query("SELECT s FROM SalonService s WHERE s.serviceId = :serviceId AND s.salonId = :salonId AND s.isActive = true")
    Optional<SalonService> findByServiceIdAndSalonId(Long serviceId, Long salonId);

    //@Query("SELECT s FROM SalonService s WHERE s.salonId = :salonId AND s.isActive = true")
    Page<SalonService> findBySalonId(Long salonId, Pageable pageable);

    //@Query("SELECT s FROM SalonService s WHERE s.salonId = :salonId AND s.categoryId = :categoryId AND s.isActive = true")
    List<SalonService> findBySalonIdAndCategoryId(Long salonId,Long categoryId);

    //@Query("SELECT s FROM SalonService s WHERE s.isActive = true")
    List<SalonService> findAll();

    List<SalonService> findAllByServiceIdIn(List<Long> ids);
}