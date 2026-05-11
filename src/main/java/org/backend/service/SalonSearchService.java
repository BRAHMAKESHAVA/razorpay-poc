package org.backend.service;

import lombok.RequiredArgsConstructor;
import org.backend.dto.SalonDetailsDTO;
import org.backend.dto.SalonSearchWithSelectedServicesResponseDTO;
import org.backend.dto.ServiceDTO;
import org.backend.dto.common.PageResponse;
import org.backend.exception.BadRequestException;
import org.backend.exception.ResourceNotFoundException;
import org.backend.model.SalonImages;
import org.backend.model.SalonService;
import org.backend.projection.NearBySalonsProjection;
import org.backend.repository.CategoryRepository;
import org.backend.repository.SalonImagesRepository;
import org.backend.repository.SalonRepository;
import org.backend.repository.SalonServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for salon search operations.
 * Handles nearby search, pagination, service-based filtering and keyword search.
 */
@Service
@RequiredArgsConstructor
public class SalonSearchService {

    private final SalonRepository salonRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final CategoryRepository categoryRepository;
    private final SalonImagesRepository salonImageRepository;

    /**
     * Finds salons near given coordinates within a distance.
     */
    // FIND NEARBY SALONS
    public List<SalonDetailsDTO> findNearbySalons(
            double latitude,
            double longitude,
            double distance,
            String unit) {

        double distanceKm = getDistanceInKm(distance, unit);

        double[] bounds = calculateBounds(latitude, longitude, distanceKm);

        List<NearBySalonsProjection> rows = salonRepository.findNearbySalons(
                latitude, longitude,
                bounds[0], bounds[1],
                bounds[2], bounds[3],
                distanceKm
        );

        List<SalonDetailsDTO> salons = rows.stream()
                .map(p -> mapToSalonDTO(p, unit))
                .toList();

        return attachImages(salons);
    }

    /**
     * Finds nearby salons with pagination.
     */
    // FIND NEARBY SALONS WITH PAGINATION
    public PageResponse<SalonDetailsDTO> findNearbySalonsWithPagination(
            double latitude,
            double longitude,
            double distance,
            String unit,
            int page,
            int size) {

        double distanceKm = getDistanceInKm(distance, unit);

        double[] bounds = calculateBounds(latitude, longitude, distanceKm);

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<NearBySalonsProjection> pageResult =
                salonRepository.findNearbySalonsWithPagination(
                        latitude, longitude,
                        bounds[0], bounds[1],
                        bounds[2], bounds[3],
                        distanceKm,
                        pageable
                );

        List<SalonDetailsDTO> salons = pageResult.getContent().stream()
                .map(p -> mapToSalonDTO(p, unit))
                .toList();

        salons = attachImages(salons);

        return PageResponse.<SalonDetailsDTO>builder()
                .page(page)
                .size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .content(salons)
                .build();
    }

    /**
     * Finds salons offering all selected services.
     */
    // FIND SALONS WITH SELECTED SERVICES
    public List<SalonSearchWithSelectedServicesResponseDTO> findSalonsWithSelectedServices(
            double latitude,
            double longitude,
            double distance,
            String unit,
            List<String> selectedServices) {

        if (selectedServices == null || selectedServices.isEmpty() ||
                selectedServices.stream().allMatch(s -> s == null || s.trim().isEmpty())) {
            throw new BadRequestException("At least one valid service must be selected.");
        }

        List<SalonDetailsDTO> nearbySalons =
                findNearbySalons(latitude, longitude, distance, unit);

        if (nearbySalons.isEmpty()) return List.of();

        List<Long> salonIds = nearbySalons.stream()
                .map(SalonDetailsDTO::getSalonId)
                .toList();

        List<SalonService> services = salonServiceRepository.findBySalonIds(salonIds);

        Set<String> requiredServices = selectedServices.stream()
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toSet());

        Map<Long, List<SalonService>> salonServiceMap = services.stream()
                .collect(Collectors.groupingBy(SalonService::getSalonId));

        return nearbySalons.stream()
                .map(salon -> buildSalonWithServices(
                        salon,
                        salonServiceMap.getOrDefault(salon.getSalonId(), List.of()),
                        requiredServices
                ))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Searches nearby salons based on keyword.
     */
    // SEARCH NEARBY SALONS
    public List<SalonDetailsDTO> searchNearbySalons(
            double latitude,
            double longitude,
            double distance,
            String unit,
            String keyword) {

        String cleanKeyword = (keyword == null) ? "" : keyword.trim();

        if (cleanKeyword.isEmpty()) return Collections.emptyList();

        double distanceKm = getDistanceInKm(distance, unit);

        double[] bounds = calculateBounds(latitude, longitude, distanceKm);

        List<NearBySalonsProjection> rows =
                salonRepository.searchNearbySalons(
                        latitude, longitude,
                        bounds[0], bounds[1],
                        bounds[2], bounds[3],
                        distanceKm,
                        cleanKeyword
                );

        Map<String, NearBySalonsProjection> uniqueMap = new LinkedHashMap<>();

        for (NearBySalonsProjection p : rows) {
            String key = p.getSalonName().toLowerCase().trim();
            uniqueMap.putIfAbsent(key, p);
        }

        return uniqueMap.values().stream()
                .map(p -> SalonDetailsDTO.builder().salonName(p.getSalonName()).build())
                .toList();
    }

    /**
     * Fetches salons by name.
     */
    // GET SALONS BY NAME
    public List<SalonDetailsDTO> getSalonsByName(
            String salonName,
            double latitude,
            double longitude,
            String unit) {

        if (salonName == null || salonName.trim().isEmpty()) {
            throw new BadRequestException("Salon name is required");
        }

        List<NearBySalonsProjection> rows =
                salonRepository.findSalonsByName(latitude, longitude, salonName.trim());

        if (rows == null || rows.isEmpty()) {
            throw new ResourceNotFoundException("Salon not found");
        }

        List<SalonDetailsDTO> salons = rows.stream()
                .map(p -> mapToSalonDTO(p, unit))
                .toList();

        return attachImages(salons);
    }


    // HELPER METHODS
    /**
     * Converts distance to kilometers.
     */
    private double getDistanceInKm(double distance, String unit) {
        return ("M".equalsIgnoreCase(unit)) ? distance / 1000.0 : distance;
    }

    /**
     * Calculates bounding box coordinates.
     */
    private double[] calculateBounds(double latitude, double longitude, double distanceKm) {
        double latDelta = distanceKm / 111.0;

        double lonDelta = distanceKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        return new double[]{
                latitude - latDelta,
                latitude + latDelta,
                longitude - lonDelta,
                longitude + lonDelta
        };
    }

    /**
     * Maps projection to DTO.
     */
    private SalonDetailsDTO mapToSalonDTO(NearBySalonsProjection p, String unit) {
        double finalDistance = ("M".equalsIgnoreCase(unit))
                ? p.getDistanceKm() * 1000
                : p.getDistanceKm();

        return SalonDetailsDTO.builder()
                .salonId(p.getSalonId())
                .partnerId(p.getPartnerId())
                .salonName(p.getSalonName())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .addressLine1(p.getAddressLine1())
                .addressLine2(p.getAddressLine2())
                .landmark(p.getLandmark())
                .city(p.getCity())
                .state(p.getState())
                .zipCode(p.getZipCode())
                .country(p.getCountry())
                .workingDays(p.getWorkingDays())
                .workingHoursStart(p.getWorkingHoursStart())
                .workingHoursEnd(p.getWorkingHoursEnd())
                .distance(Math.round(finalDistance * 100.0) / 100.0)
                .unit(unit == null ? "KM" : unit.toUpperCase())
                .build();
    }

    /**
     * Attaches images to salons.
     */
    private List<SalonDetailsDTO> attachImages(List<SalonDetailsDTO> salons) {
        List<Long> salonIds = salons.stream()
                .map(SalonDetailsDTO::getSalonId)
                .toList();

        if (salonIds.isEmpty()) return salons;

        List<SalonImages> images = salonImageRepository.findFrontViewImages(salonIds);

        Map<Long, String> imageMap = images.stream()
                .collect(Collectors.toMap(
                        SalonImages::getSalonId,
                        SalonImages::getImageUrl,
                        (e, r) -> e
                ));

        salons.forEach(salon ->
                salon.setSalonImage(imageMap.get(salon.getSalonId()))
        );

        return salons;
    }

    /**
     * Builds response for salons with selected services.
     */
    private SalonSearchWithSelectedServicesResponseDTO buildSalonWithServices(
            SalonDetailsDTO salon,
            List<SalonService> services,
            Set<String> requiredServices) {

        List<SalonService> matched = services.stream()
                .filter(s -> requiredServices.contains(s.getServiceName().toLowerCase().trim()))
                .toList();

        if (matched.size() != requiredServices.size()) {
            return null;
        }

        List<ServiceDTO> serviceDTOs = matched.stream()
                .map(s -> new ServiceDTO(s.getServiceName(), s.getPrice()))
                .toList();

        BigDecimal totalPrice = matched.stream()
                .map(SalonService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SalonSearchWithSelectedServicesResponseDTO.builder()
                .salonId(salon.getSalonId())
                .salonName(salon.getSalonName())
                .salonImageUrl(salon.getSalonImage())
                .distance(salon.getDistance())
                .unit(salon.getUnit())
                .totalPrice(totalPrice)
                .serviceDetails(serviceDTOs)
                .build();
    }
}