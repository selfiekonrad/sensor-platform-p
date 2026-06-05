package com.ceniuch.sensorqueryservice.controller;

import com.ceniuch.sensorqueryservice.dto.AlertDto;
import com.ceniuch.sensorqueryservice.dto.SensorReadingDto;
import com.ceniuch.sensorqueryservice.service.SensorQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
public class SensorQueryController {

    private final SensorQueryService queryService;

    public SensorQueryController(SensorQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/sensors/{id}/current")
    public ResponseEntity<SensorReadingDto> current(@PathVariable("id") UUID id) {
        return queryService.getCurrent(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/sensors/{id}/history")
    public List<SensorReadingDto> history(
            @PathVariable("id") UUID id,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return queryService.getHistory(id, from, to);
    }

    @GetMapping("/alerts")
    public List<AlertDto> alerts() {
        return queryService.getActiveAlerts();
    }
}