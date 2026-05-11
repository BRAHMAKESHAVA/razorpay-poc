package org.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.backend.enums.PartnerStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "salon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalonDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    @JsonBackReference  // <-- Prevents recursion
    private PartnerDetails partner;

    @JsonProperty("partnerId")   // will show in JSON
    public Long getPartnerId() {
        return partner != null ? partner.getPartnerId() : null;
    }

    @Column(nullable = false, length = 100)
    private String salonName;

    @Column(length = 20)
    private String registrationNumber;
    @Column(length = 20)
    private String gstNumber;

    @Column(length = 20)
    private Double latitude;
    @Column(length = 20)
    private Double longitude;

    @Column(nullable = false, length = 150)
    private String addressLine1;

    @Column(length = 100)
    private String addressLine2;
    @Column(length = 100)
    private String landmark;

    @Column(nullable = false, length = 25)
    private String city;

    @Column(nullable = false, length = 25)
    private String state;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(nullable = false, length = 100)
    private String workingDays;

    @Column(nullable = false, columnDefinition = "TIME DEFAULT '08:00:00'")
    private LocalTime workingHoursStart;

    @Column(nullable = false, columnDefinition = "TIME DEFAULT '20:00:00'")
    private LocalTime workingHoursEnd;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime lastModifiedDate = LocalDateTime.now();

    @Builder.Default
    private String status = PartnerStatus.CREATED.getDisplayName();  // ACTIVE, INACTIVE, PENDING
}

