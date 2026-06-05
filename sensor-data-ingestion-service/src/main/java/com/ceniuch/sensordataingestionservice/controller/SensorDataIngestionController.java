package com.ceniuch.sensordataingestionservice.controller;

import com.ceniuch.sensordataingestionservice.dtos.SensorDataResponseDto;
import com.ceniuch.sensordataingestionservice.models.SensorData;
import com.ceniuch.sensordataingestionservice.models.SensorRequest;
import com.ceniuch.sensordataingestionservice.service.SensorDataIngestionService;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensors")
@Slf4j
public class SensorDataIngestionController {

    private final SensorDataIngestionService sensorDataIngestionService;

    public SensorDataIngestionController(SensorDataIngestionService sensorDataIngestionService) {
        this.sensorDataIngestionService = sensorDataIngestionService;
    }

    @PostMapping(value = "/data", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SensorDataResponseDto> ingestSensorData(
            @Valid @RequestBody SensorData sensorData,
            @RequestHeader(value = "X-SDS-API-Key") String apiKey,
            @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor
    ) throws SensorUnauthorizedException {
        log.info("Received sensor data from sensor {} (forwardedFor={})", sensorData.sensorId(), xForwardedFor);

        SensorRequest sensorRequest = new SensorRequest(apiKey, xForwardedFor, sensorData);

        SensorDataResponseDto response = sensorDataIngestionService.ingest(sensorRequest);
        return ResponseEntity.accepted().body(response);
    }
}
