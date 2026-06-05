package com.ceniuch.sensordataprocessingservice.repository;

import com.ceniuch.db.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID> {
    boolean existsByEventId(UUID eventId);
}
