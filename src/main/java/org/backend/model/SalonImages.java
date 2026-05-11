package org.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;

@Entity
@Table(name = "salon_images", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"salon_id", "image_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalonImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;   // FK reference to SalonDetails

    @Column(name = "image_key", nullable = false, length = 50)
    private String imageKey;   // e.g. "frontView", "receptionDeck"

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;   // GCS URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageStatus status;

    @Column(length = 255)
    private String reason;  // only applicable if rejected/invalid

    public enum ImageStatus {
        CREATED, UPDATED, INVALID, REJECTED, APPROVED
    }

    @Override
    public String toString() {
        String[] arr = {""+getId(), ""+getSalonId(), getImageKey(), getImageUrl(), getReason(), getStatus().toString()};
        return Arrays.asList(arr).toString();
    }
}


