package com.ceniuch.sensormanagementservice.dto;

import java.time.Instant;
import java.util.UUID;

public record SensorRegistryResponseDto(
        UUID sensorId,
        String apiKey,
        Instant createdAt
) {
}
