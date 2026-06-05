package com.ceniuch.db.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "sensor_threshold",
        indexes = @Index(name = "idx_sensor_threshold_sensor", columnList = "sensor_id", unique = true)
)
public class SensorThreshold implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sensor_id", nullable = false, unique = true)
    private UUID sensorId;

    @Column(name = "low_threshold", nullable = false)
    private Float lowThreshold;

    @Column(name = "high_threshold", nullable = false)
    private Float highThreshold;
}
