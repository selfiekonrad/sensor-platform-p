package com.ceniuch.sensorqueryservice.service;

import com.ceniuch.db.model.Alert;
import com.ceniuch.db.model.AlertType;
import com.ceniuch.db.model.SensorReading;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import com.ceniuch.sensorqueryservice.dto.AlertDto;
import com.ceniuch.sensorqueryservice.dto.SensorReadingDto;
import com.ceniuch.sensorqueryservice.repository.AlertRepository;
import com.ceniuch.sensorqueryservice.repository.SensorReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorQueryServiceTest {

    @Mock
    private SensorReadingRepository readingRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private SensorQueryService service;

    @Test
    void getCurrent_returnsLatestReadingMapped() {
        UUID sensorId = UUID.randomUUID();
        SensorReading reading = newReading(sensorId, 22.5f, Instant.parse("2026-05-22T10:00:00Z"));
        when(readingRepository.findTopBySensorIdOrderByTimestampDesc(sensorId))
                .thenReturn(Optional.of(reading));

        Optional<SensorReadingDto> result = service.getCurrent(sensorId);

        assertThat(result).isPresent();
        assertThat(result.get().sensorId()).isEqualTo(sensorId);
        assertThat(result.get().value()).isEqualTo(22.5f);
    }

    @Test
    void getCurrent_noReading_returnsEmpty() {
        UUID sensorId = UUID.randomUUID();
        when(readingRepository.findTopBySensorIdOrderByTimestampDesc(sensorId))
                .thenReturn(Optional.empty());

        assertThat(service.getCurrent(sensorId)).isEmpty();
    }

    @Test
    void getHistory_mapsReadingsInOrder() {
        UUID sensorId = UUID.randomUUID();
        Instant from = Instant.parse("2026-05-22T00:00:00Z");
        Instant to = Instant.parse("2026-05-22T23:59:59Z");
        SensorReading r1 = newReading(sensorId, 20.0f, Instant.parse("2026-05-22T10:00:00Z"));
        SensorReading r2 = newReading(sensorId, 22.0f, Instant.parse("2026-05-22T11:00:00Z"));
        when(readingRepository.findBySensorIdAndTimestampBetweenOrderByTimestampAsc(sensorId, from, to))
                .thenReturn(List.of(r1, r2));

        List<SensorReadingDto> result = service.getHistory(sensorId, from, to);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).value()).isEqualTo(20.0f);
        assertThat(result.get(1).value()).isEqualTo(22.0f);
    }

    @Test
    void getHistory_emptyResult_returnsEmptyList() {
        UUID sensorId = UUID.randomUUID();
        Instant from = Instant.now();
        Instant to = from.plusSeconds(60);
        when(readingRepository.findBySensorIdAndTimestampBetweenOrderByTimestampAsc(sensorId, from, to))
                .thenReturn(List.of());

        assertThat(service.getHistory(sensorId, from, to)).isEmpty();
    }

    @Test
    void getActiveAlerts_mapsAlertsToDtos() {
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID());
        alert.setSensorId(UUID.randomUUID());
        alert.setAlertType(AlertType.TEMPERATURE_HIGH);
        alert.setMessage("hot");
        alert.setResolved(false);
        alert.setCreatedAt(Instant.now());
        alert.setUnit(Unit.CELSIUS);
        alert.setActualValue(99.0f);
        alert.setThresholdValue(80.0f);
        when(alertRepository.findByResolvedFalseOrderByCreatedAtDesc()).thenReturn(List.of(alert));

        List<AlertDto> result = service.getActiveAlerts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).alertType()).isEqualTo(AlertType.TEMPERATURE_HIGH);
        assertThat(result.get(0).resolved()).isFalse();
        assertThat(result.get(0).actualValue()).isEqualTo(99.0f);
    }

    private SensorReading newReading(UUID sensorId, float value, Instant ts) {
        SensorReading reading = new SensorReading();
        reading.setId(UUID.randomUUID());
        reading.setEventId(UUID.randomUUID());
        reading.setSensorId(sensorId);
        reading.setSensorType(SensorType.TEMPERATURE);
        reading.setValue(value);
        reading.setUnit(Unit.CELSIUS);
        reading.setTimestamp(ts);
        reading.setIngestedAt(ts.plusSeconds(1));
        return reading;
    }
}
