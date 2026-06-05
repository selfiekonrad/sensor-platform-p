package com.ceniuch.sensormanagementservice.model;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ThresholdRequest(
        @NotNull UUID sensorId,
        @NotNull Float lowThreshold,
        @NotNull Float highThreshold
) {
}