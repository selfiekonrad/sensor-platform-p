package com.ceniuch.sensordataingestionservice.dtos.mappers;

import com.ceniuch.sensordataingestionservice.models.SensorData;
import com.ceniuch.sensordataingestionservice.models.SensorRequest;
import com.ceniuch.common.events.SensorDataEvent;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SensorDataMapperTest {

    private final SensorDataMapper mapper = new SensorDataMapper();

    @Test
    void toEvent_copiesAllSensorFieldsAndGeneratesIdAndEnqueuedAt() {
        UUID sensorId = UUID.randomUUID();
        SensorType sensorType = SensorType.PRESSURE;
        Instant timestamp = Instant.parse("2026-05-22T10:00:00Z");
        SensorData data = new SensorData(sensorId, sensorType, 42.0f, Unit.BAR, timestamp);
        SensorRequest request = new SensorRequest("api-key", "10.0.0.1", data);

        Instant before = Instant.now();
        SensorDataEvent event = mapper.toEvent(request);
        Instant after = Instant.now();

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getSensorId()).isEqualTo(sensorId);
        assertThat(event.getSensorType()).isEqualTo(sensorType);
        assertThat(event.getValue()).isEqualTo(42.0f);
        assertThat(event.getUnit()).isEqualTo(Unit.BAR);
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getEnqueuedAt()).isBetween(before, after);
    }

    @Test
    void toEvent_generatesDistinctEventIdsAcrossCalls() {
        SensorData data = new SensorData(
                UUID.randomUUID(), SensorType.HUMIDITY, 1.0f, Unit.PERCENT, Instant.now());
        SensorRequest request = new SensorRequest("api-key", null, data);

        SensorDataEvent first = mapper.toEvent(request);
        SensorDataEvent second = mapper.toEvent(request);

        assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
    }
}