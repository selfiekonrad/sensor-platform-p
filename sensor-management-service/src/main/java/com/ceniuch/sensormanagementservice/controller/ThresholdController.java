package com.ceniuch.sensormanagementservice.controller;

import com.ceniuch.db.model.SensorThreshold;
import com.ceniuch.sensormanagementservice.model.ThresholdRequest;
import com.ceniuch.sensormanagementservice.service.ThresholdService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class ThresholdController {

    private final ThresholdService thresholdService;

    @PostMapping(value = "/threshold", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SensorThreshold> upsertThreshold(@Valid @RequestBody ThresholdRequest request) {
        log.info("Upserting threshold for sensor {}: low={}, high={}",
                request.sensorId(), request.lowThreshold(), request.highThreshold());
        return ResponseEntity.ok(thresholdService.upsert(request));
    }
}