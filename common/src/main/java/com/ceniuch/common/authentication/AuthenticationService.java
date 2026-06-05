package com.ceniuch.common.authentication;

import com.ceniuch.common.db.SensorRepository;
import com.ceniuch.common.encryption.EncryptionService;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import com.ceniuch.db.model.Sensor;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AuthenticationService {

    private final EncryptionService encryptionService;
    private final SensorRepository sensorRepository;

    public AuthenticationService(EncryptionService encryptionService, SensorRepository sensorRepository) {
        this.encryptionService = encryptionService;
        this.sensorRepository = sensorRepository;
    }

    public void authenticateSensor(UUID sensorId, String apiKey) throws SensorUnauthorizedException {
        if (sensorId == null) {
            log.info("Provided Sensor ID was null");
            throw SensorUnauthorizedException.invalidApiKey();
        }

        if (StringUtils.isEmpty(apiKey)) {
            log.info("Provided API Key was blank");
            throw SensorUnauthorizedException.invalidApiKey();
        }

        Sensor sensorIndb = sensorRepository.findById(sensorId).orElseThrow(
                SensorUnauthorizedException::sensorNotFound
        );

        String encryptedKey = encryptionService.encryptKeyForDb(apiKey);

        if (!encryptedKey.equals(sensorIndb.getApiKey())) {
            log.info("Encrypted API Key does not match expected API Key");
            throw SensorUnauthorizedException.invalidApiKey();
        }
    }
}
