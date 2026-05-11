package org.backend.repository;

import org.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.customerId = ?1 AND b.status <> 'PENDING'")
    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findBySalonId(Long salonId);

    List<Booking> findByStatus(String status);

    @Query("""
                SELECT COUNT(b) > 0 FROM Booking b
                WHERE b.salonId = :salonId
                AND b.status IN :statuses
                AND b.startTime < :end
                AND b.endTime > :start
            """)
    boolean existsOverlappingBooking(
            @Param("salonId") Long salonId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<String> statuses
    );

    @Query("""
                SELECT b FROM Booking b
                WHERE b.salonId = :salonId
                AND b.status IN :statuses
                AND b.startTime >= :startOfDay
                AND b.startTime < :endOfDay
            """)
    List<Booking> findBookingsForDate(
            @Param("salonId") Long salonId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("statuses") List<String> statuses
    );
}