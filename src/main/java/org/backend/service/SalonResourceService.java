package org.backend.service;

import lombok.RequiredArgsConstructor;
import org.backend.exception.BadRequestException;
import org.backend.exception.DuplicateResourceException;
import org.backend.exception.ResourceNotFoundException;
import org.backend.model.SalonResource;
import org.backend.repository.SalonRepository;
import org.backend.repository.SalonResourceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SalonResourceService {

    private final SalonResourceRepository resourceRepository;
    private final SalonRepository salonRepository;

    /**
     * Create a new resource for a salon.
     */
    public SalonResource createResource(SalonResource request) {
        // Verify that the salon exists
        salonRepository.findById(request.getSalonId()).orElseThrow(() -> new ResourceNotFoundException("Salon not found"));

        // Ensure only one resource record exists per salon
        if (resourceRepository.existsBySalonId(request.getSalonId())) {
            throw new DuplicateResourceException(
                    "Resource already exists for this salon. Please update the existing one."
            );
        }

        return resourceRepository.save(request);
    }

    /**
     * Retrieve a resource by salon ID.
     */
    public SalonResource getResourceBySalonId(Long salonId) {
        // Confirm salon exists before fetching resources
        salonRepository.findById(salonId).orElseThrow(() -> new ResourceNotFoundException("Salon not found"));

        return resourceRepository.findBySalonId(salonId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found for salon"));
    }

    /**
     * Update an existing resource.
     */
    public SalonResource updateResource(Long resourceId, SalonResource request) {

        if (request.getSalonId() == null) {
            throw new BadRequestException("Salon ID is required to update the resource.");
        }
        SalonResource resource =  resourceRepository.findByIdAndSalonId(resourceId, request.getSalonId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource with ID:" + resourceId + " was not found for salon:" +request.getSalonId()));

        // Update resource count if provided and valid
        if (request.getResourceCount() != null) {
            if (request.getResourceCount() < 1) throw new BadRequestException("Resource count must be >= 1");
            resource.setResourceCount(request.getResourceCount());
        }

        return resourceRepository.save(resource);
    }

    /**
     * Delete a resource by salon and resource ID.
     */
    public void deleteResource(Long salonId, Long resourceId) {
        // Ensure the resource exists for the given salon before deletion
        resourceRepository.findByIdAndSalonId(resourceId, salonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found for salonId: " + salonId + " and resourceId: " + resourceId
                ));
        resourceRepository.deleteById(resourceId);
    }
}
