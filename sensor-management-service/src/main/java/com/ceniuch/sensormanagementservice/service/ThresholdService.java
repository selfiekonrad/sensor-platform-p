package com.ceniuch.sensormanagementservice.service;

import com.ceniuch.db.model.SensorThreshold;
import com.ceniuch.sensormanagementservice.model.ThresholdRequest;
import com.ceniuch.sensormanagementservice.repository.SensorThresholdRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ThresholdService {

    private final SensorThresholdRepository thresholdRepository;

    @Transactional
    public SensorThreshold upsert(ThresholdRequest request) {
        SensorThreshold threshold = thresholdRepository.findBySensorId(request.sensorId())
                .orElseGet(SensorThreshold::new);
        threshold.setSensorId(request.sensorId());
        threshold.setLowThreshold(request.lowThreshold());
        threshold.setHighThreshold(request.highThreshold());
        return thresholdRepository.save(threshold);
    }
}