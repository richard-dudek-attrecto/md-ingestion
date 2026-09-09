package com.rddk.mdingestion.strategy;

import com.rddk.mdingestion.domain.DeviceType;
import com.rddk.mdingestion.domain.NormalizedTelemetryEvent;
import com.rddk.mdingestion.dto.TemperatureTelemetryDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * {@link DeviceIngestionStrategy} for simple numeric temperature devices. Maps a single
 * reading to exactly one {@link NormalizedTelemetryEvent}; it performs format translation
 * only and contains no lateness logic.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@Component
public class TemperatureStrategy implements DeviceIngestionStrategy<TemperatureTelemetryDto> {

    /**
     * {@inheritDoc}
     *
     * @return always {@link DeviceType#NUMERIC}
     */
    @Override
    public DeviceType getSupportedType() {
        return DeviceType.NUMERIC;
    }

    /**
     * Maps a single temperature reading to one normalized event, placing the reading
     * under the {@code temperatureValue} metric.
     *
     * @param rawData the temperature payload; must not be {@code null}
     * @return a single-element list containing the normalized event
     */
    @Override
    public List<NormalizedTelemetryEvent> normalize(TemperatureTelemetryDto rawData) {
        Objects.requireNonNull(rawData, "rawData must not be null");

        NormalizedTelemetryEvent normalizedEvent = new NormalizedTelemetryEvent(
                UUID.randomUUID().toString(),
                rawData.deviceId(),
                getSupportedType(),
                rawData.recordedAt(),
                Instant.now(),
                Map.of("temperatureValue", rawData.temperatureValue()),
                false
        );

        return List.of(normalizedEvent);
    }
}
