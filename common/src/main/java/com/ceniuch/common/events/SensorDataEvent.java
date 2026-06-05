package com.ceniuch.common.events;

import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@ToString
public class SensorDataEvent implements Serializable {
    private UUID eventId;
    private UUID sensorId;
    private SensorType sensorType;
    private Float value;
    private Unit unit;
    private Instant timestamp;
    private Instant enqueuedAt;
}