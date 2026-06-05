import { sleep } from 'k6';
import { Options } from 'k6/options';
import { SensorType } from './types.ts';
import { hosts, registerSensor, seedThreshold, sendReading, nextValue } from './lib.ts';

const POOL_SIZE = parseInt('20', 10);
const PEAK_VUS = parseInt('40', 10);
const ANOMALY_PROB = parseFloat('0.4');
const THINK = parseFloat('0.1');

const TEMPERATURE_LOW = 10;
const TEMPERATURE_HIGH = 30;

export const options: Options = {
    scenarios: {
        ingest: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: __ENV.RAMP || '30s', target: PEAK_VUS },
                { duration: __ENV.HOLD || '1m', target: PEAK_VUS },
                { duration: '15s', target: 0 },
            ],
            gracefulStop: '15s',
        },
    },
    // SLOs: the run fails (non-zero exit) if any of these are breached.
    thresholds: {
        http_req_failed: ['rate<0.01'],
        checks: ['rate>0.99'],
        ingest_duration: ['p(95)<500', 'p(99)<1000'],
        'http_req_duration{endpoint:ingest}': ['p(95)<500'],
    },
    hosts,
};

type Pool = ReturnType<typeof registerSensor>[];

// Runs once before the scenario. Builds the sensor pool and fails fast if the
// platform is unreachable so we don't report a "successful" empty run.
export function setup(): Pool {
    const pool: Pool = [];
    for (let i = 0; i < POOL_SIZE; i++) {
        const sensor = registerSensor(SensorType.TEMPERATURE);
        if (sensor && seedThreshold(sensor, TEMPERATURE_LOW, TEMPERATURE_HIGH)) {
            pool.push(sensor);
        }
    }
    if (pool.length === 0) {
        throw new Error('setup failed: could not register/seed any sensors');
    }
    console.log(`setup: ${pool.length}/${POOL_SIZE} sensors registered and seeded`);
    return pool;
}

export default function (pool: Pool) {
    const sensor = pool[Math.floor(Math.random() * pool.length)];
    if (!sensor) return;

    const { value } = nextValue(TEMPERATURE_LOW, TEMPERATURE_HIGH, ANOMALY_PROB);
    sendReading(sensor, value);

    if (THINK > 0) sleep(THINK);

}