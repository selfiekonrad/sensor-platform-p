package com.ceniuch.sensordataingestionservice.models;

import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record SensorData(
        @NotNull
        UUID sensorId,

        @NotNull
        SensorType sensorType,

        @NotNull
        Float value,

        @NotNull
        Unit unit,

        @NotNull
        Instant timestamp
) {
}
