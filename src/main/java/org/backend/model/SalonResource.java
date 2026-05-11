package org.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "salon_resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalonResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Salon ID is required")
    @Column(name = "salon_id", nullable = false)
    private Long salonId; //fk reference to salon table

    @NotNull(message = "Resource count is required")
    @Min(value = 1, message = "Resource count must be at least 1")
    @Column(name = "resource_count", nullable = false)
    private Integer resourceCount;
}