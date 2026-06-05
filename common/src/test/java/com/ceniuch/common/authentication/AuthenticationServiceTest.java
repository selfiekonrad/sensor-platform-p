package com.ceniuch.common.authentication;

import com.ceniuch.common.db.SensorRepository;
import com.ceniuch.common.encryption.EncryptionService;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import com.ceniuch.db.model.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private AuthenticationService service;

    private UUID sensorId;
    private Sensor sensor;

    @BeforeEach
    void setUp() {
        sensorId = UUID.randomUUID();
        sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setApiKey("encrypted-known-key");
    }

    @Test
    void authenticateSensor_matchingEncryptedKey_passes() {
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(encryptionService.encryptKeyForDb("plain-key")).thenReturn("encrypted-known-key");

        assertThatCode(() -> service.authenticateSensor(sensorId, "plain-key"))
                .doesNotThrowAnyException();
    }

    @Test
    void authenticateSensor_nullSensorId_throwsInvalidApiKey() {
        assertThatThrownBy(() -> service.authenticateSensor(null, "some-key"))
                .isInstanceOf(SensorUnauthorizedException.class)
                .hasMessageContaining("Api Key was invalid");

        verifyNoInteractions(sensorRepository, encryptionService);
    }

    @Test
    void authenticateSensor_nullApiKey_throwsInvalidApiKey() {
        assertThatThrownBy(() -> service.authenticateSensor(sensorId, null))
                .isInstanceOf(SensorUnauthorizedException.class)
                .hasMessageContaining("Api Key was invalid");

        verifyNoInteractions(sensorRepository, encryptionService);
    }

    @Test
    void authenticateSensor_emptyApiKey_throwsInvalidApiKey() {
        assertThatThrownBy(() -> service.authenticateSensor(sensorId, ""))
                .isInstanceOf(SensorUnauthorizedException.class);

        verifyNoInteractions(sensorRepository, encryptionService);
    }

    @Test
    void authenticateSensor_sensorNotFound_throwsSensorNotFound() {
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticateSensor(sensorId, "some-key"))
                .isInstanceOf(SensorUnauthorizedException.class)
                .hasMessageContaining("Sensor not found");
    }

    @Test
    void authenticateSensor_wrongApiKey_throwsInvalidApiKey() {
        when(sensorRepository.findById(sensorId)).thenReturn(Optional.of(sensor));
        when(encryptionService.encryptKeyForDb("wrong-key")).thenReturn("encrypted-different");

        assertThatThrownBy(() -> service.authenticateSensor(sensorId, "wrong-key"))
                .isInstanceOf(SensorUnauthorizedException.class)
                .hasMessageContaining("Api Key was invalid");
    }
}
