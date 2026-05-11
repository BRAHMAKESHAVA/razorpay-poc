package org.backend.repository;

import org.backend.model.SalonResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalonResourceRepository extends JpaRepository<SalonResource, Long> {
    boolean existsBySalonId(Long salonId);
    Optional<SalonResource> findBySalonId(Long salonId);
    Optional<SalonResource> findByIdAndSalonId(Long id, Long salonId);
}