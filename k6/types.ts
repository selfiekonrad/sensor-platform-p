// Shared types mirroring the platform's API contracts.
// k6 v2 strips TS types and transpiles enums natively, so these run as-is.

export type Sensor = {
    id: string,
    name: string,
    apiKey: string,
    sensorType: SensorType,
    createdAt: string,
};

export enum SensorType {
    TEMPERATURE = "TEMPERATURE",
    PRESSURE = "PRESSURE",
    HUMIDITY = "HUMIDITY",
}

// Mirrors com.ceniuch.db.model.Unit
export enum Unit {
    CELSIUS = "CELSIUS",
    BAR = "BAR",
    PASCAL = "PASCAL",
    PERCENT = "PERCENT",
}

// POST /api/register response (SensorRegistryResponseDto)
export type RegisterResponse = {
    sensorId: string,
    apiKey: string,
    createdAt: string,
};

// GET /api/sensors/{id}/current and entries of /history (SensorReadingDto)
export type ReadingDto = {
    id: string,
    sensorId: string,
    sensorType: SensorType,
    value: number,
    unit: Unit,
    timestamp: string,
    ingestedAt: string,
};

// Entries of GET /api/alerts (AlertDto)
export type AlertDto = {
    id: string,
    sensorId: string,
    readingId: string,
    alertType: string,
    message: string,
    thresholdValue: number,
    actualValue: number,
    unit: Unit,
    createdAt: string,
    resolved: boolean,
};
