package com.ridepulse.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByUserId(Long userId);

    @Query("SELECT d FROM DriverProfile d WHERE d.user.email = :email")
    Optional<DriverProfile> findByUserEmail(@Param("email") String email);

    List<DriverProfile> findByIsAvailableTrueAndVehicleType(VehicleType vehicleType);

    /**
     * Native MySQL Haversine query to find active, available drivers within a radius (e.g., 5.0 km)
     * of pickup coordinates, ordered by shortest distance.
     */
    @Query(value = """
        SELECT d.*, 
          (6371 * ACOS(
            LEAST(1.0, GREATEST(-1.0,
              COS(RADIANS(:pickupLat)) * COS(RADIANS(d.current_latitude)) * 
              COS(RADIANS(d.current_longitude) - RADIANS(:pickupLng)) + 
              SIN(RADIANS(:pickupLat)) * SIN(RADIANS(d.current_latitude))
            ))
          )) AS distance_in_km
        FROM driver_profiles d
        WHERE d.is_available = TRUE 
          AND d.vehicle_type = :vehicleType
          AND d.current_latitude IS NOT NULL
          AND d.current_longitude IS NOT NULL
        HAVING distance_in_km <= :searchRadiusKm
        ORDER BY distance_in_km ASC
        LIMIT :maxLimit
        """, nativeQuery = true)
    List<DriverProfile> findNearbyAvailableDrivers(
            @Param("pickupLat") double pickupLat,
            @Param("pickupLng") double pickupLng,
            @Param("vehicleType") String vehicleType,
            @Param("searchRadiusKm") double searchRadiusKm,
            @Param("maxLimit") int maxLimit
    );
}
