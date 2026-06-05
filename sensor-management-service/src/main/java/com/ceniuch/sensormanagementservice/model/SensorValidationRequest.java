package com.ceniuch.sensormanagementservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SensorValidationRequest(
        @NotNull UUID sensorId,
        @NotBlank String apiKey
) {
}