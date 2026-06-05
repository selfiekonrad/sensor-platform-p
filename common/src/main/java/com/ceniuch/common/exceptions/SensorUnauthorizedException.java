package com.ceniuch.common.exceptions;

import lombok.Getter;

@Getter
public class SensorUnauthorizedException extends Exception {

	private SensorUnauthorizedException(String message) {
		super(message);
	}

	public static SensorUnauthorizedException invalidApiKey() {
		return new SensorUnauthorizedException("Api Key was invalid.");
	}

	public static SensorUnauthorizedException sensorNotFound() {
		return new SensorUnauthorizedException("Sensor not found.");
	}
}
