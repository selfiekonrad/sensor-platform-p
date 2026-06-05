package com.ceniuch.sensormanagementservice.controller;

import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.sensormanagementservice.dto.SensorRegistryResponseDto;
import com.ceniuch.sensormanagementservice.model.SensorRegistryData;
import com.ceniuch.sensormanagementservice.model.SensorValidationRequest;
import com.ceniuch.sensormanagementservice.service.SensorRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorRegistrationControllerTest {

    @Mock
    private SensorRegistrationService registrationService;

    @InjectMocks
    private SensorRegistrationController controller;

    private UUID sensorId;

    @BeforeEach
    void setUp() {
        sensorId = UUID.randomUUID();
    }

    @Test
    void registerSensor_returns200WithResponseDto() {
        SensorRegistryData data = new SensorRegistryData("boiler-room-1", SensorType.PRESSURE);
        SensorRegistryResponseDto dto = new SensorRegistryResponseDto(
                sensorId, "generated-key", Instant.parse("2026-05-22T10:00:00Z"));
        when(registrationService.registerSensor(data)).thenReturn(dto);

        ResponseEntity<SensorRegistryResponseDto> response =
                controller.registerSensor(data, "10.0.0.1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
        verify(registrationService).registerSensor(data);
    }

    @Test
    void validateApiKey_validRequest_returns204() throws Exception {
        SensorValidationRequest request = new SensorValidationRequest(sensorId, "valid-key");
        doNothing().when(registrationService).validateApiKey(sensorId, "valid-key");

        ResponseEntity<Void> response = controller.validateApiKey(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(registrationService).validateApiKey(sensorId, "valid-key");
    }

    @Test
    void validateApiKey_unauthorized_propagatesException() throws Exception {
        SensorValidationRequest request = new SensorValidationRequest(sensorId, "bad-key");
        doThrow(SensorUnauthorizedException.invalidApiKey())
                .when(registrationService).validateApiKey(sensorId, "bad-key");

        assertThatThrownBy(() -> controller.validateApiKey(request))
                .isInstanceOf(SensorUnauthorizedException.class);
    }
}