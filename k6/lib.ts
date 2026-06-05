// Shared k6 helpers, custom metrics and configuration for the sensor platform.
//
// Everything is environment-driven so the same scripts run against the local
// gateway, a port-forward, or CI:
//   BASE_URL    full write-path base URL          (default http://<HOST>:<PORT>)
//   HOST/PORT   gateway host + LB port            (default sensor-platform.local:54101)
//   RESOLVE_IP  IP the gateway host resolves to   (default 127.0.0.1)
//   QUERY_URL   read-path (query service) base URL (default "" -> read checks skipped)
//
// The query service has no gateway route today (see project analysis), so the
// read path is opt-in: start a port-forward and pass QUERY_URL, e.g.
//   kubectl port-forward svc/sensor-query-service 8082:80
//   QUERY_URL=http://localhost:8082 k6 run smoke_test.ts

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { Sensor, SensorType, Unit, RegisterResponse, ReadingDto } from './types.ts';

const HOST = 'sensor-platform.local';
const PORT = '58412';
const RESOLVE_IP = '127.0.0.1';

export const BASE_URL = `http://${HOST}:${PORT}`;
export const QUERY_URL = '';

export const hosts: Record<string, string> = { [HOST]: RESOLVE_IP };

export const registerTrend = new Trend('register_duration', true);
export const thresholdTrend = new Trend('threshold_duration', true);
export const ingestTrend = new Trend('ingest_duration', true);
export const anomaliesSent = new Counter('anomalies_sent');
export const readingsSent = new Counter('readings_sent');

const JSON_HEADERS = { 'Content-Type': 'application/json' };

export function unitFor(type: SensorType): Unit {
    switch (type) {
        case SensorType.TEMPERATURE: return Unit.CELSIUS;
        case SensorType.PRESSURE: return Unit.BAR;
        case SensorType.HUMIDITY: return Unit.PERCENT;
        default: return Unit.CELSIUS;
    }
}

// Registers a sensor and returns it with the issued API key, or null on failure
// (callers must handle null instead of crashing the whole VU iteration).
export function registerSensor(type: SensorType): Sensor | null {
    const name = `${type.toLowerCase()}-${Date.now()}-${Math.floor(Math.random() * 1e6)}`;
    const res = http.post(`${BASE_URL}/api/register`, JSON.stringify({ name, type }), {
        headers: JSON_HEADERS,
        tags: { endpoint: 'register' },
    });
    registerTrend.add(res.timings.duration);

    const ok = check(res, { 'register -> 200': (r) => r.status === 200 });
    if (!ok) {
        return null;
    }

    const body = JSON.parse(res.body as string) as RegisterResponse;
    return {
        id: body.sensorId,
        name,
        apiKey: body.apiKey,
        sensorType: type,
        createdAt: body.createdAt,
    };
}

export function seedThreshold(sensor: Sensor, low: number, high: number): boolean {
    const res = http.post(`${BASE_URL}/api/threshold`, JSON.stringify({
        sensorId: sensor.id,
        lowThreshold: low,
        highThreshold: high,
    }), { headers: JSON_HEADERS, tags: { endpoint: 'threshold' } });
    thresholdTrend.add(res.timings.duration);
    return check(res, { 'threshold -> 200': (r) => r.status === 200 });
}

export function sendReading(sensor: Sensor, value: number): boolean {
    const res = http.post(`${BASE_URL}/api/sensors/data`, JSON.stringify({
        sensorId: sensor.id,
        sensorType: sensor.sensorType,
        value,
        unit: unitFor(sensor.sensorType),
        timestamp: new Date().toISOString(),
    }), {
        headers: { ...JSON_HEADERS, 'X-SDS-API-Key': sensor.apiKey },
        tags: { endpoint: 'ingest' },
    });
    ingestTrend.add(res.timings.duration);
    readingsSent.add(1);
    return check(res, { 'ingest -> 202': (r) => r.status === 202 });
}

// Produces either an in-range value or (with probability anomalyProb) a value
// just outside [low, high]. Anomalies are counted so the load report shows how
// many alerts the processing service is expected to raise.
export function nextValue(low: number, high: number, anomalyProb: number): { value: number, anomaly: boolean } {
    if (Math.random() < anomalyProb) {
        anomaliesSent.add(1);
        const span = (high - low) || 1;
        const value = Math.random() < 0.5
            ? low - (1 + Math.random() * span * 0.5)
            : high + (1 + Math.random() * span * 0.5);
        return { value, anomaly: true };
    }
    return { value: low + Math.random() * (high - low), anomaly: false };
}

export function getCurrent(sensorId: string) {
    return http.get(`${QUERY_URL}/api/sensors/${sensorId}/current`, { tags: { endpoint: 'query-current' } });
}

export function getHistory(sensorId: string, fromISO: string, toISO: string) {
    const qs = `from=${encodeURIComponent(fromISO)}&to=${encodeURIComponent(toISO)}`;
    return http.get(`${QUERY_URL}/api/sensors/${sensorId}/history?${qs}`, { tags: { endpoint: 'query-history' } });
}

export function getAlerts() {
    return http.get(`${QUERY_URL}/api/alerts`, { tags: { endpoint: 'query-alerts' } });
}

export function waitForCurrent(sensorId: string, timeoutSec: number = 15): ReadingDto | null {
    const deadline = Date.now() + timeoutSec * 1000;
    while (Date.now() < deadline) {
        const res = getCurrent(sensorId);
        if (res.status === 200) {
            return JSON.parse(res.body as string) as ReadingDto;
        }
        sleep(0.5);
    }
    return null;
}