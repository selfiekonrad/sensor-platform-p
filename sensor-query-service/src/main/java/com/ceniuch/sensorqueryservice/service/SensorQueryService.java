package com.ceniuch.sensorqueryservice.service;

import com.ceniuch.sensorqueryservice.dto.AlertDto;
import com.ceniuch.sensorqueryservice.dto.SensorReadingDto;
import com.ceniuch.sensorqueryservice.repository.AlertRepository;
import com.ceniuch.sensorqueryservice.repository.SensorReadingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SensorQueryService {

    private final SensorReadingRepository readingRepository;
    private final AlertRepository alertRepository;

    public Optional<SensorReadingDto> getCurrent(UUID sensorId) {
        return readingRepository.findTopBySensorIdOrderByTimestampDesc(sensorId)
                .map(SensorReadingDto::from);
    }

    public List<SensorReadingDto> getHistory(UUID sensorId, Instant from, Instant to) {
        return readingRepository
                .findBySensorIdAndTimestampBetweenOrderByTimestampAsc(sensorId, from, to)
                .stream()
                .map(SensorReadingDto::from)
                .toList();
    }

    public List<AlertDto> getActiveAlerts() {
        return alertRepository.findByResolvedFalseOrderByCreatedAtDesc()
                .stream()
                .map(AlertDto::from)
                .toList();
    }
}