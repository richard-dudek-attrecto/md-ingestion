package com.rddk.mdingestion.strategy;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.dto.TemperatureTelemetryDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for resolving the correct ingestion strategy for each device type.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
class IngestionStrategyRegistryTest {

    private final IngestionStrategyRegistry registry = new IngestionStrategyRegistry(
            List.of(new TemperatureStrategy(), new VibrationStrategy()));

    /**
     * Verifies the registry resolves the numeric strategy for a temperature payload.
     */
    @Test
    void resolvesStrategyByDeviceType() {
        var events = registry.normalize(DeviceType.NUMERIC,
                new TemperatureTelemetryDto("temp-1", 21.5, Instant.now()));

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().deviceType()).isEqualTo(DeviceType.NUMERIC);
    }

    /**
     * Verifies the registry fails fast when no strategy is available for the requested device type.
     */
    @Test
    void throwsWhenNoStrategyRegistered() {
        IngestionStrategyRegistry empty = new IngestionStrategyRegistry(List.of());

        assertThatThrownBy(() -> empty.normalize(DeviceType.BURSTY, new Object()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
