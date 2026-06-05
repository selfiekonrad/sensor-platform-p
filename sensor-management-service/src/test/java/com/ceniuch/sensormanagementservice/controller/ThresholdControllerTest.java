package com.ceniuch.sensormanagementservice.controller;

import com.ceniuch.db.model.SensorThreshold;
import com.ceniuch.sensormanagementservice.model.ThresholdRequest;
import com.ceniuch.sensormanagementservice.service.ThresholdService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThresholdControllerTest {

    @Mock
    private ThresholdService thresholdService;

    @InjectMocks
    private ThresholdController controller;

    @Test
    void upsertThreshold_returns200WithPersistedThreshold() {
        UUID sensorId = UUID.randomUUID();
        ThresholdRequest request = new ThresholdRequest(sensorId, 10f, 30f);
        SensorThreshold persisted = new SensorThreshold();
        persisted.setSensorId(sensorId);
        persisted.setLowThreshold(10f);
        persisted.setHighThreshold(30f);
        when(thresholdService.upsert(request)).thenReturn(persisted);

        ResponseEntity<SensorThreshold> response = controller.upsertThreshold(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(persisted);
        verify(thresholdService).upsert(request);
    }
}