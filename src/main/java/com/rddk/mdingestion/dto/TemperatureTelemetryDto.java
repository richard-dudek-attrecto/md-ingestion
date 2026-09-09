package com.rddk.mdingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Raw JSON payload from a simple numeric device such as a temperature sensor.
 *
 * @param deviceId         identifier of the reporting device
 * @param temperatureValue the measured temperature reading
 * @param recordedAt       time the reading was taken on the device
 * @author richard.dudek
 * @since 0.0.1
 */
public record TemperatureTelemetryDto(
        @NotBlank String deviceId,
        @NotNull Double temperatureValue,
        @NotNull Instant recordedAt
) {
}
