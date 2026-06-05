package com.ceniuch.sensorqueryservice.dto;

import com.ceniuch.db.model.SensorReading;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;

import java.time.Instant;
import java.util.UUID;

public record SensorReadingDto(
        UUID id,
        UUID sensorId,
        SensorType sensorType,
        Float value,
        Unit unit,
        Instant timestamp,
        Instant ingestedAt
) {
    public static SensorReadingDto from(SensorReading reading) {
        return new SensorReadingDto(
                reading.getId(),
                reading.getSensorId(),
                reading.getSensorType(),
                reading.getValue(),
                reading.getUnit(),
                reading.getTimestamp(),
                reading.getIngestedAt()
        );
    }
}