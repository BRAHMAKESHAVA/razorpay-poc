package org.backend.repository;

import org.backend.model.PartnerDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerRepository extends JpaRepository<PartnerDetails, Long> {

    boolean existsByMobile(String mobile);
}
