package org.backend.repository;

import org.backend.model.BookingServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingServiceEntity, Long> {

    List<BookingServiceEntity> findByBookingId(Long bookingId);

}