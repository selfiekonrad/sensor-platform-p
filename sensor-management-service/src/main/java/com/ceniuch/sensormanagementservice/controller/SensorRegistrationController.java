package com.ceniuch.sensormanagementservice.controller;

import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import com.ceniuch.sensormanagementservice.dto.SensorRegistryResponseDto;
import com.ceniuch.sensormanagementservice.model.SensorRegistryData;
import com.ceniuch.sensormanagementservice.model.SensorValidationRequest;
import com.ceniuch.sensormanagementservice.service.SensorRegistrationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class SensorRegistrationController {

    private final SensorRegistrationService registrationService;

    public SensorRegistrationController(SensorRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SensorRegistryResponseDto> registerSensor(
            @Valid @RequestBody SensorRegistryData sensorRegistryData,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor
    ) {
        log.info("Registering sensor {} from {}", sensorRegistryData, xForwardedFor);
        SensorRegistryResponseDto response = registrationService.registerSensor(sensorRegistryData);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/validate", consumes = "application/json")
    public ResponseEntity<Void> validateApiKey(@Valid @RequestBody SensorValidationRequest request)
            throws SensorUnauthorizedException {
        registrationService.validateApiKey(request.sensorId(), request.apiKey());
        return ResponseEntity.noContent().build();
    }
}