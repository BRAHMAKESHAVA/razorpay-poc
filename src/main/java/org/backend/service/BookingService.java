package org.backend.service;

import lombok.RequiredArgsConstructor;
import org.backend.dto.booking.BookingRequestDTO;
import org.backend.dto.booking.BookingResponseDTO;
import org.backend.exception.BadRequestException;
import org.backend.model.*;
import org.backend.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final BookingServiceRepository bsRepo;
    private final SalonServiceRepository serviceRepo;
    private final PaymentRepository paymentRepo;
    private final SalonRepository salonRepo;

    private static final BigDecimal PLATFORM_FEE = BigDecimal.valueOf(9);
    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.05);
    private static final BigDecimal DISCOUNT_THRESHOLD = BigDecimal.valueOf(499);
    private static final BigDecimal DISCOUNT_AMOUNT = BigDecimal.valueOf(50);

    // CREATE BOOKING
    /**
     * Creates a booking with calculated pricing and validates slot availability.
     */
    public BookingResponseDTO createBooking(Long customerId, BookingRequestDTO req) {
        List<SalonService> services = serviceRepo.findAllById(req.getServiceIds());
        BigDecimal gross = BigDecimal.ZERO;
        int totalDuration = 0;

        for (SalonService s : services) {
            gross = gross.add(s.getPrice());
            totalDuration += s.getDurationMinutes();
        }

        LocalDateTime startTime = req.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(totalDuration);

        if (!checkSlotAvailable(req.getSalonId(), startTime, endTime)) {
            throw new BadRequestException("Selected time slot is not available");
        }

        BigDecimal platformFee = gross.compareTo(BigDecimal.ZERO) > 0
                ? PLATFORM_FEE : BigDecimal.ZERO;
        BigDecimal tax = gross.multiply(TAX_RATE);
        BigDecimal discount = gross.compareTo(DISCOUNT_THRESHOLD) > 0
                ? DISCOUNT_AMOUNT : BigDecimal.ZERO;

        BigDecimal finalAmount = gross.add(platformFee).add(tax).subtract(discount);
        BigDecimal partnerAmount = gross.subtract(platformFee).subtract(tax);

        Booking booking = new Booking();
        booking.setSalonId(req.getSalonId());
        booking.setCustomerId(customerId);
        booking.setStartTime(req.getStartTime());
        booking.setEndTime(endTime);
        booking.setGrossAmount(gross);
        booking.setPlatformFee(platformFee);
        booking.setTaxAmount(tax);
        booking.setDiscountAmount(discount);
        booking.setFinalAmount(finalAmount);
        booking.setPartnerAmount(partnerAmount);
        booking.setStatus("PENDING");
        booking.setCreatedDate(LocalDateTime.now());

        booking = bookingRepo.save(booking);

        for (SalonService s : services) {
            BookingServiceEntity bs = new BookingServiceEntity();
            bs.setBookingId(booking.getBookingId());
            bs.setServiceId(s.getServiceId());
            bs.setServiceName(s.getServiceName());
            bs.setServicePrice(s.getPrice());
            bs.setServiceDuration(s.getDurationMinutes());
            bs.setSourceType("ADD_ON");
            bs.setStatus("PENDING");
            bsRepo.save(bs);
        }

        return new BookingResponseDTO(booking.getBookingId(), finalAmount, "PENDING");
    }

    // CONFIRM BOOKING
    /**
     * Confirms a booking.
     */
    public void confirmBooking(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow();
        booking.setStatus("CONFIRMED");
        booking.setUpdatedDate(LocalDateTime.now());
        bookingRepo.save(booking);
    }

    // GET USER BOOKINGS
    /**
     * Fetches bookings for a customer with salon and refund details.
     */
    public List<BookingResponseDTO> getCustomerBookings(Long userId) {
        List<Booking> bookings = bookingRepo.findByCustomerId(userId);
        List<BookingResponseDTO> response = new ArrayList<>();

        List<Long> salonIds = bookings.stream().map(Booking::getSalonId).toList();
        List<Long> bookingIds = bookings.stream().map(Booking::getBookingId).toList();

        Map<Long, SalonDetails> salonMap = salonRepo.findAllById(salonIds)
                .stream()
                .collect(Collectors.toMap(SalonDetails::getSalonId, s -> s));

        Map<Long, Payment> paymentMap = paymentRepo.findByBookingIdIn(bookingIds)
                .stream()
                .collect(Collectors.toMap(Payment::getBookingId, p -> p));

        for (Booking booking : bookings) {
            BookingResponseDTO dto = new BookingResponseDTO();
            dto.setBookingId(booking.getBookingId());
            dto.setStatus(booking.getStatus());
            dto.setStartTime(booking.getStartTime());
            dto.setCreatedDate(booking.getCreatedDate());
            dto.setFinalAmount(booking.getFinalAmount());

            SalonDetails salon = salonMap.get(booking.getSalonId());

            if (salon != null) {
                dto.setSalonName(salon.getSalonName());
            } else {
                dto.setSalonName("Salon");
            }

            Payment payment = paymentMap.get(booking.getBookingId());

            if (payment != null) {
                Map<String, String> refund = getRefundPreview(payment);
                dto.setRefundAmount(refund.get("refundAmount"));
                dto.setRefundTier(refund.get("refundTier"));
            } else {
                dto.setRefundAmount("0");
                dto.setRefundTier("NONE");
            }

            response.add(dto);
        }

        return response;
    }

    /**
     * Calculates refund amount based on time difference.
     */
    private BigDecimal calculateRefundAmount(Payment payment) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingTime = payment.getCreatedDate();

        if (bookingTime == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long diffMinutes = java.time.Duration.between(bookingTime, now).toMinutes();

        if (diffMinutes <= 10) {
            return payment.getAmount().setScale(2, RoundingMode.HALF_UP);
        }

        if (diffMinutes <= 60) {
            return payment.getAmount()
                    .multiply(new BigDecimal("0.5"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    // REFUND PREVIEW
    /**
     * Returns refund preview including amount and tier.
     */
    public Map<String, String> getRefundPreview(Payment payment) {
        BigDecimal refundAmount = calculateRefundAmount(payment);
        String tier;

        if (refundAmount.compareTo(payment.getAmount()) == 0) {
            tier = "FULL";
        } else if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            tier = "PARTIAL";
        } else {
            tier = "NONE";
        }

        Map<String, String> result = new HashMap<>();
        result.put("refundAmount", refundAmount.toPlainString());
        result.put("refundTier", tier);

        return result;
    }

    /**
     * Checks if a slot is available.
     */
    private boolean checkSlotAvailable(Long salonId, LocalDateTime start, LocalDateTime end) {

        if (start == null || end == null || !start.isBefore(end)) {
            throw new BadRequestException("Invalid time range");
        }

        LocalDateTime now = LocalDateTime.now();

        if (start.isBefore(now)) {
            return false;
        }

        if (end.isBefore(now)) {
            return false;
        }

        SalonDetails salon = salonRepo.findById(salonId).orElseThrow(() -> new RuntimeException("Salon not found"));

        LocalDateTime salonOpen = salon.getWorkingHoursStart().atDate(start.toLocalDate());

        LocalDateTime salonClose = salon.getWorkingHoursEnd().atDate(start.toLocalDate());

        if (start.isBefore(salonOpen) || end.isAfter(salonClose)) {
            return false;
        }

        List<String> activeStatuses = List.of("PENDING", "CONFIRMED", "IN_PROGRESS");

        boolean isOverlapping = bookingRepo.existsOverlappingBooking(salonId, start, end, activeStatuses);

        return !isOverlapping;
    }

    /**
     * Returns available slots for given services and date.
     */
//    public List<String> getAvailableSlots(Long salonId,
//                                          List<Long> serviceIds,
//                                          LocalDate date) {
//
//        List<SalonService> services = serviceRepo.findAllById(serviceIds);
//
//        int totalDuration = services.stream()
//                .mapToInt(SalonService::getDurationMinutes)
//                .sum();
//
//        SalonDetails salon = salonRepo.findById(salonId).orElseThrow();
//
//        LocalTime startTime = salon.getWorkingHoursStart();
//        LocalTime endTime = salon.getWorkingHoursEnd();
//
//        List<String> availableSlots = new ArrayList<>();
//
//        LocalDate today = LocalDate.now();
//        LocalTime now = LocalTime.now()
//                .withSecond(0)
//                .withNano(0);
//
//        for (LocalTime slot = startTime;
//             !slot.plusMinutes(totalDuration).isAfter(endTime);
//             slot = slot.plusMinutes(10)) {
//
//            if (date.equals(today) && slot.isBefore(now)) {
//                continue;
//            }
//
//            LocalDateTime start = slot.atDate(date);
//            LocalDateTime end = start.plusMinutes(totalDuration);
//
//            if (checkSlotAvailable(salonId, start, end)) {
//                availableSlots.add(slot.toString());
//            }
//        }
//
//        return availableSlots;
//    }

    /**
     * Returns available slots for selected services and date.
     */
    public List<String> getAvailableSlots(Long salonId,
                                          List<Long> serviceIds,
                                          LocalDate date) {

        // Fetch services
        List<SalonService> services = serviceRepo.findAllById(serviceIds);

        if (services.isEmpty()) {
            throw new BadRequestException("Services not found");
        }

        // Calculate total duration
        int totalDuration = services.stream()
                .mapToInt(SalonService::getDurationMinutes)
                .sum();

        if (totalDuration <= 0) {
            throw new BadRequestException("Invalid service duration");
        }

        // Fetch salon
        SalonDetails salon = salonRepo.findById(salonId)
                .orElseThrow(() -> new BadRequestException("Salon not found"));

        LocalTime startTime = salon.getWorkingHoursStart();
        LocalTime endTime = salon.getWorkingHoursEnd();

        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Invalid salon timings");
        }

        // Active booking statuses
        List<String> activeStatuses = List.of("PENDING", "CONFIRMED", "IN_PROGRESS");

        // Date range
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // Fetch bookings once
        List<Booking> bookings = bookingRepo.findBookingsForDate(
                salonId, startOfDay, endOfDay, activeStatuses);

        List<String> availableSlots = new ArrayList<>();

        LocalDate today = LocalDate.now();

        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        int slotInterval = 10;

        // Slot formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        // Convert to LocalDateTime
        LocalDateTime slot = startTime.atDate(date);
        LocalDateTime closingTime = endTime.atDate(date);

        // Generate slots
        while (!slot.plusMinutes(totalDuration).isAfter(closingTime)) {

            // Skip past slots
            if (date.equals(today) && slot.toLocalTime().isBefore(now)) {
                slot = slot.plusMinutes(slotInterval);
                continue;
            }

            LocalDateTime currentSlot = slot;
            LocalDateTime end = currentSlot.plusMinutes(totalDuration);

            // Check overlap
            boolean isOverlapping = bookings.stream().anyMatch(
                    booking -> booking.getStartTime().isBefore(end)
                            && booking.getEndTime().isAfter(currentSlot)
            );

            // Add slot
            if (!isOverlapping) {
                availableSlots.add(currentSlot.toLocalTime().format(formatter));
            }

            // Move next slot
            slot = slot.plusMinutes(slotInterval);
        }

        return availableSlots;
    }
}