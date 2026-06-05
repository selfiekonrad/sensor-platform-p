package com.ceniuch.sensordataingestionservice.service;

import com.ceniuch.sensordataingestionservice.auth.SensorAuthClient;
import com.ceniuch.sensordataingestionservice.config.RabbitMQConfig;
import com.ceniuch.sensordataingestionservice.dtos.SensorDataResponseDto;
import com.ceniuch.sensordataingestionservice.dtos.mappers.SensorDataMapper;
import com.ceniuch.sensordataingestionservice.models.SensorRequest;
import com.ceniuch.common.events.SensorDataEvent;
import com.ceniuch.common.exceptions.SensorUnauthorizedException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class SensorDataIngestionService {

    private final SensorDataMapper sensorDataMapper;
    private final RabbitTemplate rabbitTemplate;
    private final SensorAuthClient sensorAuthClient;

    public SensorDataIngestionService(SensorDataMapper sensorDataMapper, RabbitTemplate rabbitTemplate,
                                      SensorAuthClient sensorAuthClient) {
        this.sensorDataMapper = sensorDataMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.sensorAuthClient = sensorAuthClient;
    }

    public SensorDataResponseDto ingest(SensorRequest request) throws SensorUnauthorizedException {
        authenticateMessage(request);

        SensorDataEvent event = sensorDataMapper.toEvent(request);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SENSOR_EXCHANGE,
                "sensor.data.ingestion",
                event
        );

        log.info("Enqueued sensor event {} for sensor {}", event.getEventId(), event.getSensorId());

        return new SensorDataResponseDto(
                "ACCEPTED",
                event.getEventId().toString(),
                "Data queued for processing",
                Instant.now()
        );
    }

    private void authenticateMessage(SensorRequest request) throws ValidationException, SensorUnauthorizedException {
        if (request.apiKey() == null) {
            throw new ValidationException("API Key is required");
        }

        sensorAuthClient.validate(
                request.sensorData().sensorId(),
                request.apiKey()
        );
    }
}
