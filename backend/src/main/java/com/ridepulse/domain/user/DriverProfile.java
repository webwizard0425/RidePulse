package com.ridepulse.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DriverProfile Entity
 * Contains vehicle details, live geospatial coordinates, availability status, and driver ratings.
 */
@Entity
@Table(
    name = "driver_profiles",
    indexes = {
        @Index(name = "idx_drivers_avail_loc", columnList = "is_available, current_latitude, current_longitude")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "vehicle_model", nullable = false, length = 50)
    private String vehicleModel; // e.g. "Honda City", "Hyundai i20"

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 30)
    private String vehicleNumber; // e.g. "DL-01-AB-1234"

    @Builder.Default
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    @Builder.Default
    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(5.00);

    @Builder.Default
    @Column(name = "total_rides_completed", nullable = false)
    private Integer totalRidesCompleted = 0;
}
