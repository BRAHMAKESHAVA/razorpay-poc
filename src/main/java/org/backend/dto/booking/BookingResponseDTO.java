package org.backend.dto.booking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponseDTO {
    private Long bookingId;
    private BigDecimal finalAmount;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime createdDate;
    private String salonName;


    //NEW FIELDS (for refund preview)
    private String refundAmount;
    private String refundTier;

    public BookingResponseDTO(Long bookingId, BigDecimal finalAmount, String status) {
        this.bookingId = bookingId;
        this.finalAmount = finalAmount;
        this.status = status;
    }
}