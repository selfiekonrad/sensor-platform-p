package com.ceniuch.sensormanagementservice.model;

import com.ceniuch.db.model.SensorType;

public record SensorRegistryData(
        String name,
        SensorType type
) {
}
