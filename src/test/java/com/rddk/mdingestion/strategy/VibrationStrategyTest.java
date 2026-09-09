package com.rddk.mdingestion.strategy;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import com.rddk.mdingestion.dto.VibrationTelemetryDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the bursty vibration normalization strategy.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
class VibrationStrategyTest {

    private final VibrationStrategy strategy = new VibrationStrategy();

    /**
     * Verifies each sample in a vibration burst becomes its own normalized event.
     */
    @Test
    void fansOutEachSampleIntoItsOwnEvent() {
        Instant recordedAt = Instant.parse("2026-01-01T00:00:00Z");
        VibrationTelemetryDto dto =
                new VibrationTelemetryDto("vib-1", List.of(1.0, 2.0, 3.0), recordedAt);

        List<NormalizedTelemetryEvent> events = strategy.normalize(dto);

        assertThat(events).hasSize(3);
        assertThat(events).allSatisfy(event -> {
            assertThat(event.deviceId()).isEqualTo("vib-1");
            assertThat(event.deviceType()).isEqualTo(DeviceType.BURSTY);
            assertThat(event.metrics()).containsEntry("sampleCount", 3);
        });
        assertThat(events.get(0).metrics()).containsEntry("sampleValue", 1.0);
        assertThat(events.get(2).timestamp()).isEqualTo(recordedAt.plusMillis(200));
    }

    /**
     * Verifies an empty vibration burst yields no normalized events.
     */
    @Test
    void emptyBurstProducesNoEvents() {
        VibrationTelemetryDto dto =
                new VibrationTelemetryDto("vib-1", List.of(), Instant.now());

        assertThat(strategy.normalize(dto)).isEmpty();
    }
}
