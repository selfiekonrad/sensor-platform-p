package com.ceniuch.db.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "sensor_reading",
        indexes = {
                @Index(name = "idx_sensor_reading_sensor_ts", columnList = "sensor_id, timestamp"),
                @Index(name = "idx_sensor_reading_timestamp", columnList = "timestamp")
        }
)
public class SensorReading implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", length = 32)
    private SensorType sensorType;

    @Column(nullable = false)
    private Float value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Unit unit;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;
}