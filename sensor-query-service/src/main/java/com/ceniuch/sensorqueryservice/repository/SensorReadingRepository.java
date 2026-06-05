package com.ceniuch.sensorqueryservice.repository;

import com.ceniuch.db.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID> {

    Optional<SensorReading> findTopBySensorIdOrderByTimestampDesc(UUID sensorId);

    List<SensorReading> findBySensorIdAndTimestampBetweenOrderByTimestampAsc(
            UUID sensorId, Instant from, Instant to);
}