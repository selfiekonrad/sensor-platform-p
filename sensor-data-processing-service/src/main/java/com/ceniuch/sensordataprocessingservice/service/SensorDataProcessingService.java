package com.ceniuch.sensordataprocessingservice.service;

import com.ceniuch.sensordataprocessingservice.anomaly.ThresholdAnomalyDetector;
import com.ceniuch.sensordataprocessingservice.config.RabbitMQConfig;
import com.ceniuch.sensordataprocessingservice.repository.AlertRepository;
import com.ceniuch.sensordataprocessingservice.repository.SensorReadingRepository;
import com.ceniuch.common.events.SensorDataEvent;
import com.ceniuch.db.model.SensorReading;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@AllArgsConstructor
public class SensorDataProcessingService {

    private final SensorReadingRepository readingRepository;
    private final AlertRepository alertRepository;
    private final ThresholdAnomalyDetector anomalyDetector;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.SENSOR_QUEUE)
    public void onSensorEvent(SensorDataEvent event) {
        if (event.getEventId() == null || event.getSensorId() == null || event.getValue() == null) {
            log.warn("Skipping malformed event: {}", event);
            return;
        }

        if (readingRepository.existsByEventId(event.getEventId())) {
            log.debug("Duplicate event {} ignored", event.getEventId());
            return;
        }

        SensorReading reading = toReading(event);
        SensorReading saved = readingRepository.save(reading);
        log.info("Persisted reading {} for sensor {}", saved.getId(), saved.getSensorId());

        anomalyDetector.check(saved).ifPresent(alert -> {
            alertRepository.save(alert);
            log.info("Alert raised: sensor={}, type={}, value={}",
                    alert.getSensorId(), alert.getAlertType(), alert.getActualValue());
        });
    }

    private SensorReading toReading(SensorDataEvent event) {
        SensorReading reading = new SensorReading();
        reading.setEventId(event.getEventId());
        reading.setSensorId(event.getSensorId());
        reading.setSensorType(event.getSensorType());
        reading.setValue(event.getValue());
        reading.setUnit(event.getUnit());
        reading.setTimestamp(event.getTimestamp());
        reading.setIngestedAt(Instant.now());
        return reading;
    }
}
