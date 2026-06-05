package com.ceniuch.sensordataingestionservice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SensorDataResponseDto(
        @NotNull(message = "status cannot be null")
        @NotBlank(message = "status cannot be blank")
        String status,

        @NotNull
        String requestId,

        @NotNull
        String message,

        @NotNull
        Instant acceptedAt
) {
}
