package com.ceniuch.sensormanagementservice.repository;

import com.ceniuch.db.model.SensorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensorThresholdRepository extends JpaRepository<SensorThreshold, UUID> {
    Optional<SensorThreshold> findBySensorId(UUID sensorId);
}