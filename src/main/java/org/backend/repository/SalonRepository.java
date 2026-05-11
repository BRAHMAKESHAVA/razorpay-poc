package org.backend.repository;

import org.backend.model.SalonDetails;
import org.backend.projection.NearBySalonsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalonRepository extends JpaRepository<SalonDetails, Long> {
    /**
     * Finds nearby salons based on the provided latitude, longitude, and distance.
     *
     * @param lat        the latitude of the user's location
     * @param lon        the longitude of the user's location
     * @param minLat     the minimum latitude for the bounding box
     * @param maxLat     the maximum latitude for the bounding box
     * @param minLon     the minimum longitude for the bounding box
     * @param maxLon     the maximum longitude for the bounding box
     * @param distanceKm the maximum distance in kilometers to consider a salon as nearby
     * @return a page of nearby salons matching the criteria
     */
    @Query(value = """
                SELECT *
                FROM (
                    SELECT 
                        s.salon_id AS salonId,
                        s.partner_id AS partnerId,
                        s.salon_name AS salonName,
                        s.latitude AS latitude,
                        s.longitude AS longitude,
                        s.address_line1 AS addressLine1,
                        s.address_line2 AS addressLine2,
                        s.landmark AS landmark,
                        s.city AS city,
                        s.state AS state,
                        s.zip_code AS zipCode,
                        s.country AS country,
                        s.working_days AS workingDays,
                        s.working_hours_start AS workingHoursStart,
                        s.working_hours_end AS workingHoursEnd,
                        (6371 * acos(
                            cos(radians(:lat)) * cos(radians(s.latitude)) *
                            cos(radians(s.longitude) - radians(:lon)) +
                            sin(radians(:lat)) * sin(radians(s.latitude))
                        )) AS distanceKm
                    FROM salon s
                    WHERE s.latitude BETWEEN :minLat AND :maxLat
                      AND s.longitude BETWEEN :minLon AND :maxLon
                ) AS nearby
                WHERE nearby.distanceKm <= :distanceKm
                ORDER BY nearby.distanceKm ASC
            """, nativeQuery = true)
    List<NearBySalonsProjection> findNearbySalons(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon,
            @Param("distanceKm") double distanceKm
    );

    /**
     * Native query to find nearby salons with pagination support.
     * The query calculates the distance of each salon from the given coordinates
     * and filters results based on the specified distance. It also includes a count query
     * for pagination purposes.
     *
     * @param lat        the latitude of the search location
     * @param lon        the longitude of the search location
     * @param minLat     the minimum latitude for bounding box filtering
     * @param maxLat     the maximum latitude for bounding box filtering
     * @param minLon     the minimum longitude for bounding box filtering
     * @param maxLon     the maximum longitude for bounding box filtering
     * @param distanceKm the maximum distance in kilometers to filter nearby salons
     * @param pageable   the pagination information (page number, page size, sorting)
     * @return a page of NearBySalonsProjection containing nearby salons with their details and distance
     */
    @Query(value = """
                SELECT *
                FROM (
                    SELECT 
                        s.salon_id AS salonId,
                        s.partner_id AS partnerId,
                        s.salon_name AS salonName,
                        s.latitude AS latitude,
                        s.longitude AS longitude,
                        s.address_line1 AS addressLine1,
                        s.address_line2 AS addressLine2,
                        s.landmark AS landmark,
                        s.city AS city,
                        s.state AS state,
                        s.zip_code AS zipCode,
                        s.country AS country,
                        s.working_days AS workingDays,
                        s.working_hours_start AS workingHoursStart,
                        s.working_hours_end AS workingHoursEnd,
                        (6371 * acos(
                            cos(radians(:lat)) * cos(radians(s.latitude)) *
                            cos(radians(s.longitude) - radians(:lon)) +
                            sin(radians(:lat)) * sin(radians(s.latitude))
                        )) AS distanceKm
                    FROM salon s
                    WHERE s.latitude BETWEEN :minLat AND :maxLat
                      AND s.longitude BETWEEN :minLon AND :maxLon
                ) AS nearby
                WHERE nearby.distanceKm <= :distanceKm
            """,
            countQuery = """
                        SELECT COUNT(*) 
                        FROM (
                            SELECT 
                                (6371 * acos(
                                    cos(radians(:lat)) * cos(radians(s.latitude)) *
                                    cos(radians(s.longitude) - radians(:lon)) +
                                    sin(radians(:lat)) * sin(radians(s.latitude))
                                )) AS distanceKm
                            FROM salon s
                            WHERE s.latitude BETWEEN :minLat AND :maxLat
                              AND s.longitude BETWEEN :minLon AND :maxLon
                        ) AS nearby
                        WHERE nearby.distanceKm <= :distanceKm
                    """,
            nativeQuery = true)
    Page<NearBySalonsProjection> findNearbySalonsWithPagination(
            double lat,
            double lon,
            double minLat,
            double maxLat,
            double minLon,
            double maxLon,
            double distanceKm,
            Pageable pageable
    );

    @Query(value = """
            SELECT *
            FROM (
                SELECT 
                    s.salon_id AS salonId,
                    s.salon_name AS salonName,
                    (6371 * acos(
                        cos(radians(:lat)) * cos(radians(s.latitude)) *
                        cos(radians(s.longitude) - radians(:lon)) +
                        sin(radians(:lat)) * sin(radians(s.latitude))
                    )) AS distanceKm
                FROM salon s
                WHERE s.latitude BETWEEN :minLat AND :maxLat
                  AND s.longitude BETWEEN :minLon AND :maxLon
                  AND LOWER(s.salon_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ) AS nearby
            WHERE nearby.distanceKm <= :distanceKm
            ORDER BY nearby.distanceKm ASC
            LIMIT 10
            """, nativeQuery = true)
    List<NearBySalonsProjection> searchNearbySalons(
            double lat,
            double lon,
            double minLat,
            double maxLat,
            double minLon,
            double maxLon,
            double distanceKm,
            String keyword
    );


    @Query(value = """
        SELECT *
        FROM (
            SELECT 
                s.salon_id AS salonId,
                s.partner_id AS partnerId,
                s.salon_name AS salonName,
                s.latitude AS latitude,
                s.longitude AS longitude,
                s.address_line1 AS addressLine1,
                s.address_line2 AS addressLine2,
                s.landmark AS landmark,
                s.city AS city,
                s.state AS state,
                s.zip_code AS zipCode,
                s.country AS country,
                s.working_days AS workingDays,
                s.working_hours_start AS workingHoursStart,
                s.working_hours_end AS workingHoursEnd,
                (6371 * acos(
                    cos(radians(:lat)) * cos(radians(s.latitude)) *
                    cos(radians(s.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(s.latitude))
                )) AS distanceKm
            FROM salon s
            WHERE LOWER(s.salon_name) = LOWER(:salonName)
        ) AS nearby
        ORDER BY nearby.distanceKm ASC
        """, nativeQuery = true)
    List<NearBySalonsProjection> findSalonsByName(
            double lat,
            double lon,
            String salonName
    );
}

