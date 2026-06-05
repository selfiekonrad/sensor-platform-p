package com.ceniuch.sensordataprocessingservice.repository;

import com.ceniuch.db.model.SensorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SensorThresholdRepository extends JpaRepository<SensorThreshold, UUID> {
    Optional<SensorThreshold> findBySensorId(UUID sensorId);
}
