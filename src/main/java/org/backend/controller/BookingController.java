package org.backend.controller;

import lombok.RequiredArgsConstructor;
import org.backend.dto.booking.BookingRequestDTO;
import org.backend.dto.booking.BookingResponseDTO;
import org.backend.dto.common.ApiResponse;
import org.backend.model.Booking;
import org.backend.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;


    // CREATE BOOKING
    @PostMapping
    public BookingResponseDTO createBooking(@RequestBody BookingRequestDTO req) {
        return bookingService.createBooking(10L, req);
    }

    // GET USER BOOKINGS
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<BookingResponseDTO>> getUserBookings(@PathVariable Long customerId) {

        List<BookingResponseDTO> bookings =
                bookingService.getCustomerBookings(customerId);

        return ApiResponse.<List<BookingResponseDTO>>builder()
                .status(true)
                .message("Bookings fetched successfully")
                .data(bookings)
                .build();
    }

    @GetMapping("/available-slots")
    public List<String> getAvailableSlots(
            @RequestParam Long salonId,
            @RequestParam List<Long> serviceIds,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return bookingService.getAvailableSlots(
                salonId,
                serviceIds,
                date
        );
    }
}