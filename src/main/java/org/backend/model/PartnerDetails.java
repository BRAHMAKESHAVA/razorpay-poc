package org.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.backend.enums.PartnerStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "partner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partnerId;

    @NotBlank(message = "User ID cannot be null or empty")
    @Column(nullable = false)
    private String userId; // refers users table id

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 15)
    private String mobile;

    @Column(length = 100)
    private String email;

    private String idProofType;       // Aadhaar, PAN, etc.
    private String idProofNumber;

    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime lastModifiedDate = LocalDateTime.now();

    @Builder.Default
    private String status = PartnerStatus.CREATED.getDisplayName();  // ACTIVE, INACTIVE, PENDING

    // One Partner can have many salons
    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // <-- Owner of serialization
    private List<SalonDetails> salons;

    public List<SalonDetails> getSalons() { return salons; }
    public void setSalons(List<SalonDetails> salons) {
        this.salons = salons;
        if (salons != null) {
            salons.forEach(s -> s.setPartner(this));
        }
    }

    @PrePersist
    @PreUpdate
    public void formatFields() {
        if (this.idProofType != null) this.idProofType = this.idProofType.toUpperCase();
        if (this.status != null) this.status = this.status.toLowerCase();
    }
}

