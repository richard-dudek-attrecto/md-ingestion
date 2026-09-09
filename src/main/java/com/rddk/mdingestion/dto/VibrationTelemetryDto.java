package com.rddk.mdingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/**
 * Raw JSON payload from a bursty device such as a vibration sensor, carrying a batch
 * of samples in a single message.
 *
 * @param deviceId   identifier of the reporting device
 * @param samples    ordered vibration samples captured in the burst
 * @param recordedAt time the burst started on the device
 * @author richard.dudek
 * @since 0.0.1
 */
public record VibrationTelemetryDto(
        @NotBlank String deviceId,
        @NotNull List<Double> samples,
        @NotNull Instant recordedAt
) {
}
