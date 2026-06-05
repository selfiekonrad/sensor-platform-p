package com.ceniuch.sensordataprocessingservice.anomaly;

import com.ceniuch.db.model.Alert;
import com.ceniuch.db.model.AlertType;
import com.ceniuch.db.model.SensorReading;
import com.ceniuch.db.model.SensorThreshold;
import com.ceniuch.db.model.Unit;
import com.ceniuch.sensordataprocessingservice.repository.SensorThresholdRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@AllArgsConstructor
public class ThresholdAnomalyDetector {

    private final SensorThresholdRepository thresholdRepository;

    public Optional<Alert> check(SensorReading reading) {
        if (reading.getValue() == null || reading.getUnit() == null || reading.getSensorId() == null) {
            return Optional.empty();
        }

        Optional<SensorThreshold> thresholdOpt = thresholdRepository.findBySensorId(reading.getSensorId());
        if (thresholdOpt.isEmpty()) {
            return Optional.empty();
        }
        SensorThreshold threshold = thresholdOpt.get();

        float value = reading.getValue();
        float low = threshold.getLowThreshold();
        float high = threshold.getHighThreshold();

        if (value > high) {
            return Optional.of(buildAlert(reading, highAlertTypeFor(reading.getUnit()), high, value,
                    "Value %.2f exceeded upper threshold %.2f".formatted(value, high)));
        }
        if (value < low) {
            return Optional.of(buildAlert(reading, lowAlertTypeFor(reading.getUnit()), low, value,
                    "Value %.2f fell below lower threshold %.2f".formatted(value, low)));
        }
        return Optional.empty();
    }

    private AlertType highAlertTypeFor(Unit unit) {
        return switch (unit) {
            case CELSIUS -> AlertType.TEMPERATURE_HIGH;
            case BAR, PASCAL -> AlertType.PRESSURE_HIGH;
            case PERCENT -> AlertType.OUT_OF_RANGE;
        };
    }

    private AlertType lowAlertTypeFor(Unit unit) {
        return switch (unit) {
            case CELSIUS -> AlertType.TEMPERATURE_LOW;
            case BAR, PASCAL -> AlertType.PRESSURE_LOW;
            case PERCENT -> AlertType.OUT_OF_RANGE;
        };
    }

    private Alert buildAlert(SensorReading reading, AlertType type, float threshold, float actual, String message) {
        Alert alert = new Alert();
        alert.setSensorId(reading.getSensorId());
        alert.setReadingId(reading.getId());
        alert.setAlertType(type);
        alert.setMessage(message);
        alert.setThresholdValue(threshold);
        alert.setActualValue(actual);
        alert.setUnit(reading.getUnit());
        alert.setCreatedAt(Instant.now());
        alert.setResolved(false);
        return alert;
    }
}
