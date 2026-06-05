package com.ceniuch.sensormanagementservice.service;

import com.ceniuch.common.authentication.AuthenticationService;
import com.ceniuch.common.db.SensorRepository;
import com.ceniuch.common.encryption.EncryptionService;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import com.ceniuch.db.model.Sensor;
import com.ceniuch.sensormanagementservice.dto.SensorRegistryResponseDto;
import com.ceniuch.sensormanagementservice.model.SensorRegistryData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SensorRegistrationService {

    private final EncryptionService encryptionService;
    private final SensorRepository sensorRepository;
    private final AuthenticationService authenticationService;

    public SensorRegistryResponseDto registerSensor(SensorRegistryData sensorRegistryData) {
        String plainApiKey = generateApiKey();

        Sensor sensor = new Sensor();
        sensor.setSensorType(sensorRegistryData.type());
        sensor.setName(sensorRegistryData.name());
        sensor.setApiKey(encryptionService.encryptKeyForDb(plainApiKey));
        sensor.setCreatedAt(Instant.now());

        sensorRepository.save(sensor);

        return new SensorRegistryResponseDto(sensor.getId(), plainApiKey, sensor.getCreatedAt());
    }

    public void validateApiKey(UUID sensorId, String apiKey) throws SensorUnauthorizedException {
        authenticationService.authenticateSensor(sensorId, apiKey);
    }

    private String generateApiKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}