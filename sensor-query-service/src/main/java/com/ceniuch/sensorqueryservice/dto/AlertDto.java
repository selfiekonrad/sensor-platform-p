package com.ceniuch.sensorqueryservice.dto;

import com.ceniuch.db.model.Alert;
import com.ceniuch.db.model.AlertType;
import com.ceniuch.db.model.Unit;

import java.time.Instant;
import java.util.UUID;

public record AlertDto(
        UUID id,
        UUID sensorId,
        UUID readingId,
        AlertType alertType,
        String message,
        Float thresholdValue,
        Float actualValue,
        Unit unit,
        Instant createdAt,
        boolean resolved
) {
    public static AlertDto from(Alert alert) {
        return new AlertDto(
                alert.getId(),
                alert.getSensorId(),
                alert.getReadingId(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getThresholdValue(),
                alert.getActualValue(),
                alert.getUnit(),
                alert.getCreatedAt(),
                alert.isResolved()
        );
    }
}