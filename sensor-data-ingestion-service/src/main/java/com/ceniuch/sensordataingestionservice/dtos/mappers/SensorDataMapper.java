package com.ceniuch.sensordataingestionservice.dtos.mappers;

import com.ceniuch.sensordataingestionservice.models.SensorRequest;
import com.ceniuch.common.events.SensorDataEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class SensorDataMapper {

    public SensorDataEvent toEvent(SensorRequest request) {
        SensorDataEvent event = new SensorDataEvent();
        event.setEventId(UUID.randomUUID());
        event.setSensorId(request.sensorData().sensorId());
        event.setSensorType(request.sensorData().sensorType());
        event.setValue(request.sensorData().value());
        event.setUnit(request.sensorData().unit());
        event.setTimestamp(request.sensorData().timestamp());
        event.setEnqueuedAt(Instant.now());
        return event;
    }
}
