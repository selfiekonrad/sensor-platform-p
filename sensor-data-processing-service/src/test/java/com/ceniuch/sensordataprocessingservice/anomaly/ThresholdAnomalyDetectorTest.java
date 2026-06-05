package com.ceniuch.sensordataprocessingservice.anomaly;

import com.ceniuch.db.model.Alert;
import com.ceniuch.db.model.AlertType;
import com.ceniuch.db.model.SensorReading;
import com.ceniuch.db.model.SensorThreshold;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import com.ceniuch.sensordataprocessingservice.repository.SensorThresholdRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThresholdAnomalyDetectorTest {

    @Mock
    private SensorThresholdRepository thresholdRepository;

    @InjectMocks
    private ThresholdAnomalyDetector detector;

    @Test
    void check_nullValue_returnsEmpty() {
        SensorReading reading = newReading(null, Unit.CELSIUS, UUID.randomUUID());

        assertThat(detector.check(reading)).isEmpty();
    }

    @Test
    void check_nullUnit_returnsEmpty() {
        SensorReading reading = newReading(20.0f, null, UUID.randomUUID());

        assertThat(detector.check(reading)).isEmpty();
    }

    @Test
    void check_nullSensorId_returnsEmpty() {
        SensorReading reading = newReading(20.0f, Unit.CELSIUS, null);

        assertThat(detector.check(reading)).isEmpty();
    }

    @Test
    void check_noThresholdConfigured_returnsEmpty() {
        UUID sensorId = UUID.randomUUID();
        SensorReading reading = newReading(200.0f, Unit.CELSIUS, sensorId);
        when(thresholdRepository.findBySensorId(sensorId)).thenReturn(Optional.empty());

        assertThat(detector.check(reading)).isEmpty();
    }

    @Test
    void check_valueAboveHighThreshold_celsiusAlert() {
        UUID sensorId = UUID.randomUUID();
        SensorReading reading = newReading(85.0f, Unit.CELSIUS, sensorId);
        when(thresholdRepository.findBySensorId(sensorId))
                .thenReturn(Optional.of(threshold(sensorId, -20f, 80f)));

        Optional<Alert> result = detector.check(reading);

        assertThat(result).isPresent();
        Alert alert = result.get();
        assertThat(alert.getAlertType()).isEqualTo(AlertType.TEMPERATURE_HIGH);
        assertThat(alert.getActualValue()).isEqualTo(85.0f);
        assertThat(alert.getThresholdValue()).isEqualTo(80.0f);
        assertThat(alert.getUnit()).isEqualTo(Unit.CELSIUS);
        assertThat(alert.getSensorId()).isEqualTo(sensorId);
        assertThat(alert.getReadingId()).isEqualTo(reading.getId());
        assertThat(alert.isResolved()).isFalse();
        assertThat(alert.getMessage()).contains("exceeded upper threshold");
        assertThat(alert.getCreatedAt()).isNotNull();
    }

    @Test
    void check_valueBelowLowThreshold_celsiusAlert() {
        UUID sensorId = UUID.randomUUID();
        SensorReading reading = newReading(-25.0f, Unit.CELSIUS, sensorId);
        when(thresholdRepository.findBySensorId(sensorId))
                .thenReturn(Optional.of(threshold(sensorId, -20f, 80f)));

        Optional<Alert> result = detector.check(reading);

        assertThat(result).isPresent();
        Alert alert = result.get();
        assertThat(alert.getAlertType()).isEqualTo(AlertType.TEMPERATURE_LOW);
        assertThat(alert.getActualValue()).isEqualTo(-25.0f);
        assertThat(alert.getThresholdValue()).isEqualTo(-20.0f);
        assertThat(alert.getMessage()).contains("fell below lower threshold");
    }

    @Test
    void check_valueAtBoundary_returnsEmpty() {
        UUID sensorId = UUID.randomUUID();
        SensorReading reading = newReading(80.0f, Unit.CELSIUS, sensorId);
        when(thresholdRepository.findBySensorId(sensorId))
                .thenReturn(Optional.of(threshold(sensorId, -20f, 80f)));

        assertThat(detector.check(reading)).isEmpty();
    }

    @Test
    void check_valueWithinRange_returnsEmpty() {
        UUID sensorId = UUID.randomUUID();
        SensorReading reading = newReading(20.0f, Unit.CELSIUS, sensorId);
        when(thresholdRepository.findBySensorId(sensorId))
                .thenReturn(Optional.of(threshold(sensorId, -20f, 80f)));

        assertThat(detector.check(reading)).isEmpty();
    }

    @Test
    void check_bar_returnsPressureAlertTypes() {
        UUID sensorId = UUID.randomUUID();
        SensorReading reading = newReading(15.0f, Unit.BAR, sensorId);
        when(thresholdRepository.findBySensorId(sensorId))
                .thenReturn(Optional.of(threshold(sensorId, 0f, 10f)));

        assertThat(detector.check(reading))
                .map(Alert::getAlertType)
                .contains(AlertType.PRESSURE_HIGH);
    }

    @Test
    void check_pascal_belowThreshold_returnsPressureLow() {
        UUID sensorId = UUID.randomUUID();
        SensorReading reading = newReading(-1.0f, Unit.PASCAL, sensorId);
        when(thresholdRepository.findBySensorId(sensorId))
                .thenReturn(Optional.of(threshold(sensorId, 0f, 10f)));

        assertThat(detector.check(reading))
                .map(Alert::getAlertType)
                .contains(AlertType.PRESSURE_LOW);
    }

    @Test
    void check_percent_outOfRangeAboveAndBelow() {
        UUID sensorId = UUID.randomUUID();
        when(thresholdRepository.findBySensorId(sensorId))
                .thenReturn(Optional.of(threshold(sensorId, 0f, 100f)));

        SensorReading high = newReading(120.0f, Unit.PERCENT, sensorId);
        SensorReading low = newReading(-5.0f, Unit.PERCENT, sensorId);

        assertThat(detector.check(high))
                .map(Alert::getAlertType)
                .contains(AlertType.OUT_OF_RANGE);
        assertThat(detector.check(low))
                .map(Alert::getAlertType)
                .contains(AlertType.OUT_OF_RANGE);
    }

    private SensorReading newReading(Float value, Unit unit, UUID sensorId) {
        SensorReading reading = new SensorReading();
        reading.setId(UUID.randomUUID());
        reading.setEventId(UUID.randomUUID());
        reading.setSensorId(sensorId);
        reading.setSensorType(unit == Unit.PERCENT ? SensorType.HUMIDITY
                : unit == Unit.CELSIUS ? SensorType.TEMPERATURE : SensorType.PRESSURE);
        reading.setValue(value);
        reading.setUnit(unit);
        reading.setTimestamp(Instant.parse("2026-05-22T10:00:00Z"));
        reading.setIngestedAt(Instant.parse("2026-05-22T10:00:01Z"));
        return reading;
    }

    private SensorThreshold threshold(UUID sensorId, float low, float high) {
        SensorThreshold t = new SensorThreshold();
        t.setId(UUID.randomUUID());
        t.setSensorId(sensorId);
        t.setLowThreshold(low);
        t.setHighThreshold(high);
        return t;
    }
}
