package com.ceniuch.sensordataingestionservice.controller;

import com.ceniuch.sensordataingestionservice.dtos.SensorDataResponseDto;
import com.ceniuch.sensordataingestionservice.models.SensorData;
import com.ceniuch.sensordataingestionservice.models.SensorRequest;
import com.ceniuch.sensordataingestionservice.service.SensorDataIngestionService;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensorDataIngestionControllerTest {

    @Mock
    private SensorDataIngestionService ingestionService;

    @InjectMocks
    private SensorDataIngestionController controller;

    private SensorData sensorData;

    @BeforeEach
    void setUp() {
        sensorData = new SensorData(
                UUID.randomUUID(),
                SensorType.TEMPERATURE,
                22.5f,
                Unit.CELSIUS,
                Instant.parse("2026-05-22T10:00:00Z")
        );
    }

    @Test
    void ingestSensorData_successfulIngestion_returns202WithBody() throws Exception {
        SensorDataResponseDto dto = new SensorDataResponseDto(
                "ACCEPTED",
                UUID.randomUUID().toString(),
                "Data queued for processing",
                Instant.parse("2026-05-22T10:00:00Z")
        );
        when(ingestionService.ingest(any(SensorRequest.class))).thenReturn(dto);

        ResponseEntity<SensorDataResponseDto> response =
                controller.ingestSensorData(sensorData, "valid-api-key", "10.0.0.1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(dto);

        ArgumentCaptor<SensorRequest> captor = ArgumentCaptor.forClass(SensorRequest.class);
        verify(ingestionService).ingest(captor.capture());
        SensorRequest forwarded = captor.getValue();
        assertThat(forwarded.apiKey()).isEqualTo("valid-api-key");
        assertThat(forwarded.xForwardedFor()).isEqualTo("10.0.0.1");
        assertThat(forwarded.sensorData()).isEqualTo(sensorData);
    }

    @Test
    void ingestSensorData_serviceThrowsUnauthorized_propagatesException() throws Exception {
        when(ingestionService.ingest(any(SensorRequest.class)))
                .thenThrow(SensorUnauthorizedException.invalidApiKey());

        assertThatThrownBy(() -> controller.ingestSensorData(sensorData, "bad-key", null))
                .isInstanceOf(SensorUnauthorizedException.class);
    }

    @Test
    void ingestSensorData_xForwardedForIsOptional_serviceReceivesNull() throws Exception {
        SensorDataResponseDto dto = new SensorDataResponseDto(
                "ACCEPTED", UUID.randomUUID().toString(), "ok", Instant.now());
        when(ingestionService.ingest(any(SensorRequest.class))).thenReturn(dto);

        controller.ingestSensorData(sensorData, "valid-api-key", null);

        ArgumentCaptor<SensorRequest> captor = ArgumentCaptor.forClass(SensorRequest.class);
        verify(ingestionService).ingest(captor.capture());
        assertThat(captor.getValue().xForwardedFor()).isNull();
    }
}