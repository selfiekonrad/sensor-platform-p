package com.ceniuch.db.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "alert"
)
public class Alert implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Column(name = "reading_id")
    private UUID readingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 32)
    private AlertType alertType;

    @Column(nullable = false, length = 512)
    private String message;

    @Column(name = "threshold_value")
    private Float thresholdValue;

    @Column(name = "actual_value")
    private Float actualValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Unit unit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean resolved;
}