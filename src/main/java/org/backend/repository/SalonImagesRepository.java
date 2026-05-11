package org.backend.repository;

import org.backend.model.SalonImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalonImagesRepository extends JpaRepository<SalonImages, Long> {

    @Query("SELECT si FROM SalonImages si WHERE si.salonId IN :salonIds AND si.imageKey = 'frontView'")
    List<SalonImages> findFrontViewImages(@Param("salonIds") List<Long> salonIds);

    @Query("SELECT si FROM SalonImages si WHERE si.salonId = :salonId AND si.imageKey = 'frontView'")
    Optional<SalonImages> findFrontViewImage(@Param("salonId") Long salonId);
}

