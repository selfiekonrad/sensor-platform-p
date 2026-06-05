import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    iterations: 1,
    hosts: {
        'sensor-platform.local': '127.0.0.1',
    },
};

export default function () {
    const payload = JSON.stringify({
        sensorId: "8b6106bb-917e-4593-88f2-d9159ab18228",
        sensorType: "TEMPERATURE",
        value: 23.5,
        unit: "CELSIUS",
        timestamp: "2026-05-20T12:00:00.000Z"
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-SDS-API-Key': 'exQI1Kf8WVqqlqbR30Vf3frYrwR_NV7o6xxgV4y7t5A',
        },
    };

    const res = http.post(
        'http://sensor-platform.local:54101/api/sensors/data',
        payload,
        params
    );

    check(res, { "status is 202": (r) => r.status === 202 });
    sleep(1);
}

export function register_sensors() {

}
