//   register -> seed threshold -> ingest one in-range + one anomalous reading
//   -> (if QUERY_URL set) verify the read path reflects both, including the alert.

import { check, fail } from 'k6';
import { Options } from 'k6/options';
import { SensorType } from './types.ts';
import {
    hosts, QUERY_URL,
    registerSensor, seedThreshold, sendReading,
    waitForCurrent, getHistory, getAlerts,
} from './lib.ts';

const LOW = 10;
const HIGH = 30;

export const options: Options = {
    vus: 1,
    iterations: 1,
    thresholds: {
        checks: ['rate>0.99'],
        http_req_failed: ['rate<0.01'],
    },
    hosts,
};

export default function () {
    const sensor = registerSensor(SensorType.TEMPERATURE);
    if (!sensor) {
        fail('registration failed — aborting smoke test');
    }
    check(sensor, {
        'received sensorId': (s) => !!s!.id,
        'received apiKey': (s) => !!s!.apiKey,
    });

    seedThreshold(sensor!, LOW, HIGH);

    // One healthy reading and one above the high threshold (expected to alert).
    sendReading(sensor!, (LOW + HIGH) / 2);
    sendReading(sensor!, HIGH + 25);

    if (!QUERY_URL) {
        console.log('QUERY_URL not set -> skipping read-path checks (current/history/alerts).');
        return;
    }

    // Processing is async over RabbitMQ, so poll until the reading lands.
    const reading = waitForCurrent(sensor!.id, 15);
    check(reading, {
        'current reading became visible': (r) => r !== null,
        'current reading belongs to sensor': (r) => r !== null && r.sensorId === sensor!.id,
    });

    const from = new Date(Date.now() - 3600_000).toISOString();
    const to = new Date(Date.now() + 3600_000).toISOString();
    const hist = getHistory(sensor!.id, from, to);
    check(hist, {
        'history -> 200': (r) => r.status === 200,
        'history has >= 2 readings': (r) => (JSON.parse(r.body as string) as unknown[]).length >= 2,
    });

    const alerts = getAlerts();
    check(alerts, {
        'alerts -> 200': (r) => r.status === 200,
        'alert raised for sensor': (r) =>
            (JSON.parse(r.body as string) as Array<{ sensorId: string }>).some((a) => a.sensorId === sensor!.id),
    });
}