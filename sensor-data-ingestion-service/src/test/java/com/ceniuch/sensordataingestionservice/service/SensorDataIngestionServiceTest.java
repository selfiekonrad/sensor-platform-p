package com.ceniuch.sensordataingestionservice.service;

import com.ceniuch.sensordataingestionservice.auth.SensorAuthClient;
import com.ceniuch.sensordataingestionservice.config.RabbitMQConfig;
import com.ceniuch.sensordataingestionservice.dtos.SensorDataResponseDto;
import com.ceniuch.sensordataingestionservice.dtos.mappers.SensorDataMapper;
import com.ceniuch.sensordataingestionservice.models.SensorData;
import com.ceniuch.sensordataingestionservice.models.SensorRequest;
import com.ceniuch.common.events.SensorDataEvent;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class
SensorDataIngestionServiceTest {

    @Mock
    private SensorDataMapper sensorDataMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SensorAuthClient sensorAuthClient;

    @InjectMocks
    private SensorDataIngestionService service;

    private SensorData sensorData;
    private SensorDataEvent event;

    @BeforeEach
    void setUp() {
        sensorData = new SensorData(
                UUID.randomUUID(),
                SensorType.TEMPERATURE,
                23.5f,
                Unit.CELSIUS,
                Instant.parse("2026-05-22T10:00:00Z")
        );
        event = new SensorDataEvent();
        event.setEventId(UUID.randomUUID());
        event.setSensorId(sensorData.sensorId());
        event.setSensorType(sensorData.sensorType());
        event.setValue(sensorData.value());
        event.setUnit(sensorData.unit());
        event.setTimestamp(sensorData.timestamp());
        event.setEnqueuedAt(Instant.now());
    }

    @Test
    void ingest_validRequest_enqueuesEventAndReturnsAcceptedResponse() throws Exception {
        SensorRequest request = new SensorRequest("valid-api-key", "10.0.0.1", sensorData);
        when(sensorDataMapper.toEvent(request)).thenReturn(event);

        SensorDataResponseDto response = service.ingest(request);

        verify(sensorAuthClient).validate(sensorData.sensorId(), "valid-api-key");
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.SENSOR_EXCHANGE),
                eq("sensor.data.ingestion"),
                eq(event)
        );
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.requestId()).isEqualTo(event.getEventId().toString());
        assertThat(response.message()).isEqualTo("Data queued for processing");
        assertThat(response.acceptedAt()).isNotNull();
    }

    @Test
    void ingest_nullApiKey_throwsValidationException() {
        SensorRequest request = new SensorRequest(null, "10.0.0.1", sensorData);

        assertThatThrownBy(() -> service.ingest(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("API Key is required");

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    @Test
    void ingest_invalidApiKey_propagatesUnauthorizedAndDoesNotEnqueue() throws Exception {
        SensorRequest request = new SensorRequest("bad-key", null, sensorData);
        doThrow(SensorUnauthorizedException.invalidApiKey())
                .when(sensorAuthClient).validate(sensorData.sensorId(), "bad-key");

        assertThatThrownBy(() -> service.ingest(request))
                .isInstanceOf(SensorUnauthorizedException.class);

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }
}