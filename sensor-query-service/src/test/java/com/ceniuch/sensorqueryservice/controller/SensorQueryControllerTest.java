package com.ceniuch.sensorqueryservice.controller;

import com.ceniuch.db.model.AlertType;
import com.ceniuch.db.model.SensorType;
import com.ceniuch.db.model.Unit;
import com.ceniuch.sensorqueryservice.dto.AlertDto;
import com.ceniuch.sensorqueryservice.dto.SensorReadingDto;
import com.ceniuch.sensorqueryservice.service.SensorQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorQueryControllerTest {

    @Mock
    private SensorQueryService queryService;

    @InjectMocks
    private SensorQueryController controller;

    @Test
    void current_existingSensor_returns200WithDto() {
        UUID sensorId = UUID.randomUUID();
        SensorReadingDto dto = new SensorReadingDto(
                UUID.randomUUID(), sensorId, SensorType.TEMPERATURE, 22.5f, Unit.CELSIUS,
                Instant.parse("2026-05-22T10:00:00Z"), Instant.parse("2026-05-22T10:00:01Z"));
        when(queryService.getCurrent(sensorId)).thenReturn(Optional.of(dto));

        ResponseEntity<SensorReadingDto> response = controller.current(sensorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void current_missingSensor_returns404() {
        UUID sensorId = UUID.randomUUID();
        when(queryService.getCurrent(sensorId)).thenReturn(Optional.empty());

        ResponseEntity<SensorReadingDto> response = controller.current(sensorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void history_returnsListFromService() {
        UUID sensorId = UUID.randomUUID();
        Instant from = Instant.parse("2026-05-22T00:00:00Z");
        Instant to = Instant.parse("2026-05-22T23:59:59Z");
        SensorReadingDto dto = new SensorReadingDto(
                UUID.randomUUID(), sensorId, SensorType.TEMPERATURE, 21.0f, Unit.CELSIUS,
                Instant.parse("2026-05-22T12:00:00Z"), Instant.parse("2026-05-22T12:00:01Z"));
        when(queryService.getHistory(sensorId, from, to)).thenReturn(List.of(dto));

        List<SensorReadingDto> result = controller.history(sensorId, from, to);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void alerts_returnsListFromService() {
        AlertDto alertDto = new AlertDto(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                AlertType.TEMPERATURE_HIGH, "hot", 80.0f, 99.0f, Unit.CELSIUS,
                Instant.parse("2026-05-22T10:00:00Z"), false);
        when(queryService.getActiveAlerts()).thenReturn(List.of(alertDto));

        List<AlertDto> result = controller.alerts();

        assertThat(result).containsExactly(alertDto);
    }
}
