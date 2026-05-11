package org.backend.dto.booking;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingRequestDTO {
    private Long salonId;
    private List<Long> serviceIds;
    private LocalDateTime startTime;
    //private LocalDateTime endTime;
}