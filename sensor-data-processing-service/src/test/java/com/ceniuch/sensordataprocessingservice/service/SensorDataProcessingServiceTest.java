package com.ceniuch.sensordataprocessingservice.service;

import com.ceniuch.sensordataprocessingservice.anomaly.ThresholdAnomalyDetector;
import com.ceniuch.sensordataprocessingservice.repository.AlertRepository;
import com.ceniuch.sensordataprocessingservice.repository.SensorReadingRepository;
import com.ceniuch.common.events.SensorDataEvent;
import com.ceniuch.db.model.Alert;
import com.ceniuch.db.model.AlertType;
import com.ceniuch.db.model.SensorReading;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorDataProcessingServiceTest {

    @Mock
    private SensorReadingRepository readingRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private ThresholdAnomalyDetector anomalyDetector;

    @InjectMocks
    private SensorDataProcessingService service;

    private SensorDataEvent event;

    @BeforeEach
    void setUp() {
        event = new SensorDataEvent();
        event.setEventId(UUID.randomUUID());
        event.setSensorId(UUID.randomUUID());
        event.setSensorType(SensorType.TEMPERATURE);
        event.setValue(25.0f);
        event.setUnit(Unit.CELSIUS);
        event.setTimestamp(Instant.parse("2026-05-22T10:00:00Z"));
        event.setEnqueuedAt(Instant.parse("2026-05-22T10:00:01Z"));
    }

    @Test
    void onSensorEvent_validEventWithoutAnomaly_persistsReadingAndSkipsAlert() {
        when(readingRepository.existsByEventId(event.getEventId())).thenReturn(false);
        SensorReading saved = new SensorReading();
        saved.setId(UUID.randomUUID());
        saved.setSensorId(event.getSensorId());
        when(readingRepository.save(any(SensorReading.class))).thenReturn(saved);
        when(anomalyDetector.check(saved)).thenReturn(Optional.empty());

        service.onSensorEvent(event);

        ArgumentCaptor<SensorReading> captor = ArgumentCaptor.forClass(SensorReading.class);
        verify(readingRepository).save(captor.capture());
        SensorReading persisted = captor.getValue();
        assertThat(persisted.getEventId()).isEqualTo(event.getEventId());
        assertThat(persisted.getSensorId()).isEqualTo(event.getSensorId());
        assertThat(persisted.getSensorType()).isEqualTo(event.getSensorType());
        assertThat(persisted.getValue()).isEqualTo(25.0f);
        assertThat(persisted.getUnit()).isEqualTo(Unit.CELSIUS);
        assertThat(persisted.getTimestamp()).isEqualTo(event.getTimestamp());
        assertThat(persisted.getIngestedAt()).isNotNull();
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void onSensorEvent_validEventWithAnomaly_persistsReadingAndAlert() {
        when(readingRepository.existsByEventId(event.getEventId())).thenReturn(false);
        SensorReading saved = new SensorReading();
        saved.setId(UUID.randomUUID());
        when(readingRepository.save(any(SensorReading.class))).thenReturn(saved);
        Alert alert = new Alert();
        alert.setAlertType(AlertType.TEMPERATURE_HIGH);
        alert.setActualValue(85.0f);
        when(anomalyDetector.check(saved)).thenReturn(Optional.of(alert));

        service.onSensorEvent(event);

        verify(readingRepository).save(any(SensorReading.class));
        verify(alertRepository).save(alert);
    }

    @Test
    void onSensorEvent_duplicateEvent_doesNotPersistAgain() {
        when(readingRepository.existsByEventId(event.getEventId())).thenReturn(true);

        service.onSensorEvent(event);

        verify(readingRepository, never()).save(any(SensorReading.class));
        verifyNoInteractions(anomalyDetector, alertRepository);
    }

    @Test
    void onSensorEvent_nullEventId_isSkipped() {
        event.setEventId(null);

        service.onSensorEvent(event);

        verifyNoInteractions(readingRepository, anomalyDetector, alertRepository);
    }

    @Test
    void onSensorEvent_nullSensorId_isSkipped() {
        event.setSensorId(null);

        service.onSensorEvent(event);

        verifyNoInteractions(readingRepository, anomalyDetector, alertRepository);
    }

    @Test
    void onSensorEvent_nullValue_isSkipped() {
        event.setValue(null);

        service.onSensorEvent(event);

        verifyNoInteractions(readingRepository, anomalyDetector, alertRepository);
    }
}