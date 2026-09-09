package com.rddk.mdingestion.pipeline;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the per-device watermark lateness policy.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
class WatermarkLateArrivalPolicyTest {

    private final WatermarkLateArrivalPolicy policy =
            new WatermarkLateArrivalPolicy(Duration.ofSeconds(30));

    /**
     * Creates a minimal telemetry event with the given device id and timestamp.
     *
     * @param deviceId the device identifier
     * @param timestamp the event timestamp
     * @return a normalized telemetry event used by the policy tests
     */
    private NormalizedTelemetryEvent eventAt(String deviceId, Instant timestamp) {
        return new NormalizedTelemetryEvent(
                UUID.randomUUID().toString(),
                deviceId,
                DeviceType.NUMERIC,
                timestamp,
                Instant.now(),
                Map.of(),
                false);
    }

    /**
     * Verifies the first event for a device advances the watermark and is considered on time.
     */
    @Test
    void firstEventAdvancesWatermarkAndIsOnTime() {
        Instant now = Instant.parse("2026-01-01T00:00:10Z");

        NormalizedTelemetryEvent result = policy.evaluate(eventAt("dev-1", now));

        assertThat(result.isLate()).isFalse();
        assertThat(result.metrics()).containsEntry("lateness", "ON_TIME");
    }

    /**
     * Verifies an event slightly older than the watermark but within tolerance is marked out of order.
     */
    @Test
    void slightlyOutOfOrderWithinToleranceIsFlaggedOutOfOrder() {
        Instant base = Instant.parse("2026-01-01T00:00:10Z");
        policy.evaluate(eventAt("dev-1", base));

        NormalizedTelemetryEvent result = policy.evaluate(eventAt("dev-1", base.minusSeconds(5)));

        assertThat(result.isLate()).isFalse();
        assertThat(result.metrics()).containsEntry("lateness", "OUT_OF_ORDER");
    }

    /**
     * Verifies events beyond the allowed lateness window are flagged as late.
     */
    @Test
    void eventBeyondAllowedLatenessIsFlaggedLate() {
        Instant base = Instant.parse("2026-01-01T00:00:40Z");
        policy.evaluate(eventAt("dev-1", base));

        NormalizedTelemetryEvent result = policy.evaluate(eventAt("dev-1", base.minusSeconds(31)));

        assertThat(result.isLate()).isTrue();
        assertThat(result.metrics()).containsEntry("lateness", "LATE");
    }

    /**
     * Verifies the watermark is maintained independently for each device.
     */
    @Test
    void watermarksAreTrackedPerDevice() {
        Instant base = Instant.parse("2026-01-01T00:00:40Z");
        policy.evaluate(eventAt("dev-1", base));

        NormalizedTelemetryEvent otherDevice =
                policy.evaluate(eventAt("dev-2", base.minusSeconds(60)));

        assertThat(otherDevice.metrics()).containsEntry("lateness", "ON_TIME");
    }
}
