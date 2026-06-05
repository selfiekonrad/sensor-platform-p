package com.ceniuch.sensormanagementservice.service;

import com.ceniuch.db.model.SensorThreshold;
import com.ceniuch.sensormanagementservice.model.ThresholdRequest;
import com.ceniuch.sensormanagementservice.repository.SensorThresholdRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThresholdServiceTest {

    @Mock
    private SensorThresholdRepository thresholdRepository;

    @InjectMocks
    private ThresholdService service;

    @Test
    void upsert_createsNewThreshold_whenNoneExistsForSensor() {
        UUID sensorId = UUID.randomUUID();
        ThresholdRequest request = new ThresholdRequest(sensorId, 10f, 30f);
        when(thresholdRepository.findBySensorId(sensorId)).thenReturn(Optional.empty());
        when(thresholdRepository.save(any(SensorThreshold.class))).thenAnswer(inv -> inv.getArgument(0));

        SensorThreshold result = service.upsert(request);

        ArgumentCaptor<SensorThreshold> captor = ArgumentCaptor.forClass(SensorThreshold.class);
        verify(thresholdRepository).save(captor.capture());
        SensorThreshold saved = captor.getValue();
        assertThat(saved.getSensorId()).isEqualTo(sensorId);
        assertThat(saved.getLowThreshold()).isEqualTo(10f);
        assertThat(saved.getHighThreshold()).isEqualTo(30f);
        assertThat(result).isSameAs(saved);
    }

    @Test
    void upsert_updatesExistingThreshold_preservingItsId() {
        UUID sensorId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        SensorThreshold existing = new SensorThreshold();
        existing.setId(existingId);
        existing.setSensorId(sensorId);
        existing.setLowThreshold(0f);
        existing.setHighThreshold(5f);
        when(thresholdRepository.findBySensorId(sensorId)).thenReturn(Optional.of(existing));
        when(thresholdRepository.save(any(SensorThreshold.class))).thenAnswer(inv -> inv.getArgument(0));

        ThresholdRequest request = new ThresholdRequest(sensorId, 15f, 40f);
        SensorThreshold result = service.upsert(request);

        verify(thresholdRepository).save(existing);
        assertThat(result.getId()).isEqualTo(existingId);
        assertThat(result.getSensorId()).isEqualTo(sensorId);
        assertThat(result.getLowThreshold()).isEqualTo(15f);
        assertThat(result.getHighThreshold()).isEqualTo(40f);
    }
}