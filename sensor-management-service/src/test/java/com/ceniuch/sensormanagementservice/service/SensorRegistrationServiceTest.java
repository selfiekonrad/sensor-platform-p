package com.ceniuch.sensormanagementservice.service;

import com.ceniuch.common.authentication.AuthenticationService;
import com.ceniuch.common.db.SensorRepository;
import com.ceniuch.common.encryption.EncryptionService;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import com.ceniuch.db.model.Sensor;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.sensormanagementservice.dto.SensorRegistryResponseDto;
import com.ceniuch.sensormanagementservice.model.SensorRegistryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorRegistrationServiceTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private SensorRegistrationService service;

    @Test
    void registerSensor_persistsEncryptedKeyAndReturnsPlainKey() {
        SensorRegistryData input = new SensorRegistryData("temperature-room-1", SensorType.TEMPERATURE);
        when(encryptionService.encryptKeyForDb(anyString())).thenAnswer(inv -> "encrypted-" + inv.getArgument(0));

        SensorRegistryResponseDto response = service.registerSensor(input);

        ArgumentCaptor<Sensor> captor = ArgumentCaptor.forClass(Sensor.class);
        verify(sensorRepository).save(captor.capture());
        Sensor persisted = captor.getValue();
        assertThat(persisted.getName()).isEqualTo("temperature-room-1");
        assertThat(persisted.getSensorType()).isEqualTo(input.type());
        assertThat(persisted.getApiKey()).startsWith("encrypted-");
        assertThat(persisted.getCreatedAt()).isNotNull();

        assertThat(response.apiKey()).isNotBlank();
        assertThat(response.createdAt()).isEqualTo(persisted.getCreatedAt());
        assertThat(persisted.getApiKey()).isEqualTo("encrypted-" + response.apiKey());
    }

    @Test
    void registerSensor_generatesDistinctApiKeysAcrossCalls() {
        SensorRegistryData input = new SensorRegistryData("temp", SensorType.TEMPERATURE);
        when(encryptionService.encryptKeyForDb(anyString())).thenAnswer(inv -> inv.getArgument(0));

        SensorRegistryResponseDto first = service.registerSensor(input);
        SensorRegistryResponseDto second = service.registerSensor(input);

        assertThat(first.apiKey()).isNotEqualTo(second.apiKey());
    }

    @Test
    void validateApiKey_delegatesToAuthenticationService() throws Exception {
        UUID sensorId = UUID.randomUUID();

        service.validateApiKey(sensorId, "some-key");

        verify(authenticationService).authenticateSensor(sensorId, "some-key");
    }

    @Test
    void validateApiKey_propagatesUnauthorizedException() throws Exception {
        UUID sensorId = UUID.randomUUID();
        doThrow(SensorUnauthorizedException.invalidApiKey())
                .when(authenticationService).authenticateSensor(sensorId, "bad-key");

        assertThatThrownBy(() -> service.validateApiKey(sensorId, "bad-key"))
                .isInstanceOf(SensorUnauthorizedException.class);
    }
}